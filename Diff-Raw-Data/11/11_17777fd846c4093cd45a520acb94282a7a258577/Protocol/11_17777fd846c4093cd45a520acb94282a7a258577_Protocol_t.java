 package module.protocols.domain;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import module.fileManagement.domain.AbstractFileNode;
 import module.fileManagement.domain.DirNode;
 import module.fileManagement.domain.Document;
 import module.fileManagement.domain.FileNode;
 import module.geography.domain.Country;
 import module.protocols.domain.util.ProtocolAction;
 import module.protocols.domain.util.ProtocolResponsibleType;
 import module.protocols.dto.ProtocolCreationBean;
 import module.protocols.dto.ProtocolCreationBean.ProtocolResponsibleBean;

import org.joda.time.LocalDate;

 import pt.ist.bennu.core.domain.User;
 import pt.ist.bennu.core.domain.exceptions.DomainException;
 import pt.ist.bennu.core.domain.groups.AnyoneGroup;
 import pt.ist.bennu.core.domain.groups.PersistentGroup;
 import pt.ist.bennu.core.domain.groups.UnionGroup;
 import pt.ist.bennu.core.util.BundleUtil;
 import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.utl.ist.fenix.tools.util.StringNormalizer;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Ordering;
 
 /**
  * 
  * @author Joao Carvalho (joao.pedro.carvalho@ist.utl.pt)
  * 
  */
 public class Protocol extends Protocol_Base {
 
     public static enum RenewTime implements IPresentableEnum {
 	YEARS, MONTHS;
 
 	@Override
 	public String getLocalizedName() {
 	    return BundleUtil.getStringFromResourceBundle("resources/ProtocolsResources", "label.renewTime." + name());
 	}
     }
 
     public Protocol() {
 	super();
 	super.setAllowedToRead(new UnionGroup(ProtocolManager.getInstance().getAdministrativeGroup()));
 	setProtocolManager(ProtocolManager.getInstance());
 	super.setProtocolNumber(new LocalDate().getYear() + "-" + ProtocolManager.getInstance().getNewProtocolNumber());
 	setActive(Boolean.TRUE);
     }
 
     public void delete() {
 	for (ProtocolHistory history : getProtocolHistories())
 	    history.delete();
 
 	removeProtocolManager();
 	deleteDomainObject();
     }
 
     @Override
     public void setAllowedToRead(UnionGroup group) {
 	throw new RuntimeException("Groups should not be set directly!");
     }
 
     public boolean isActive() {
 	if (!getActive())
 	    return false;
 	else
 	    return getCurrentProtocolHistory() != null;
     }
 
     public ProtocolHistory getCurrentProtocolHistory() {
 	for (ProtocolHistory protocolHistory : getProtocolHistories()) {
 	    if (protocolHistory.isActive())
 		return protocolHistory;
 	}
 	return null;
     }
 
     public ProtocolHistory getLastProtocolHistory() {
 
 	return Ordering.from(ProtocolHistory.COMPARATOR_BY_END_DATE).max(getProtocolHistories());
 
     }
 
     public ProtocolHistory getPresentableProtocolHistory() {
 	ProtocolHistory current = null;
 	if (!getActive()) {
 	    current = getLastProtocolHistory();
 	} else {
 	    current = getCurrentProtocolHistory();
 	    if (current == null)
 		current = getLastProtocolHistory();
 	}
 	return current;
     }
 
     public List<ProtocolHistory> getCurrentAndFutureProtocolHistories() {
 
 	return Ordering.from(ProtocolHistory.COMPARATOR_BY_BEGIN_DATE).sortedCopy(
 		Iterables.filter(getProtocolHistories(), new Predicate<ProtocolHistory>() {
 
 		    @Override
 		    public boolean apply(ProtocolHistory history) {
 			return !history.isPast();
 		    }
 
 		}));
 
     }
 
     public boolean hasExternalPartner(final String partnerName) {
 
 	final String name = StringNormalizer.normalize(partnerName);
 
 	return Iterables.any(getProtocolResponsible(), new Predicate<ProtocolResponsible>() {
 
 	    @Override
 	    public boolean apply(ProtocolResponsible responsible) {
 
 		if (responsible.getType() != ProtocolResponsibleType.EXTERNAL)
 		    return false;
 
 		String unitName = StringNormalizer.normalize(responsible.getUnit().getPartyName().getContent());
 
 		return unitName.equals(name);
 	    }
 	});
     }
 
     @Service
     public void renewFor(Integer duration, RenewTime renewTime) {
 
 	LocalDate beginDate = getLastProtocolHistory().getEndDate();
 	LocalDate endDate = beginDate;
 	if (renewTime == RenewTime.YEARS) {
 	    endDate = endDate.plusYears(duration);
 	} else if (renewTime == RenewTime.MONTHS) {
 	    endDate = endDate.plusMonths(duration);
 	}
 
 	new ProtocolHistory(this, beginDate, endDate);
     }
 
     @Service
     public static Protocol createProtocol(ProtocolCreationBean protocolBean) {
 
 	Protocol protocol = new Protocol();
 
 	protocol.updateFromBean(protocolBean);
 
 	return protocol;
     }
 
     @Override
     public void setProtocolNumber(String number) {
 	for (Protocol protocol : ProtocolManager.getInstance().getProtocols()) {
 	    if (protocol.equals(this))
 		continue;
 	    if (protocol.getProtocolNumber().equals(number))
 		throw new DomainException("error.protocol.number.already.exists", number);
 	}
 
 	super.setProtocolNumber(number);
     }
 
     @Service
     public void updateFromBean(ProtocolCreationBean protocolBean) {
 
 	this.setSignedDate(protocolBean.getSignedDate());
 	this.setScientificAreas(protocolBean.getScientificAreas());
 	this.setProtocolAction(new ProtocolAction(protocolBean.getActionTypes(), protocolBean.getOtherActionTypes()));
 	this.setObservations(protocolBean.getObservations());
 
 	if (protocolBean.getRemovedResponsibles() != null) {
 	    for (ProtocolResponsible responsible : protocolBean.getRemovedResponsibles()) {
 		if (responsible != null)
 		    this.removeProtocolResponsible(responsible);
 	    }
 	}
 
 	for (ProtocolResponsibleBean bean : protocolBean.getInternalResponsibles()) {
 	    ProtocolResponsible responsible = bean.getProtocolResponsible();
 	    if (this.hasProtocolResponsible(responsible))
 		responsible.updateFromBean(bean);
 	    else {
 		ProtocolResponsible newResponsible = new ProtocolResponsible(ProtocolResponsibleType.INTERNAL);
 		newResponsible.updateFromBean(bean);
 
 		this.addProtocolResponsible(newResponsible);
 	    }
 	}
 
 	for (ProtocolResponsibleBean bean : protocolBean.getExternalResponsibles()) {
 	    ProtocolResponsible responsible = bean.getProtocolResponsible();
 	    if (this.hasProtocolResponsible(responsible))
 		responsible.updateFromBean(bean);
 	    else {
 		ProtocolResponsible newResponsible = new ProtocolResponsible(ProtocolResponsibleType.EXTERNAL);
 		newResponsible.updateFromBean(bean);
 
 		this.addProtocolResponsible(newResponsible);
 	    }
 	}
 
 	for (PersistentGroup group : getReaderGroups())
 	    removeReaderGroups(group);
 
 	for (PersistentGroup group : protocolBean.getReaders())
 	    addReaderGroups(group);
 
 	this.setWriterGroup(protocolBean.getWriters());
 
 	this.setVisibilityType(protocolBean.getVisibilityType());
 
 	ProtocolHistory currentProtocolHistory = getCurrentProtocolHistory();
 
 	if (currentProtocolHistory != null) {
 	    currentProtocolHistory.setBeginDate(protocolBean.getBeginDate());
 	    currentProtocolHistory.setEndDate(protocolBean.getEndDate());
 	} else {
 	    new ProtocolHistory(this, protocolBean.getBeginDate(), protocolBean.getEndDate());
 	}
 
 	generateProtocolDir();
 
     }
 
     @Override
     public void addReaderGroups(PersistentGroup group) {
	super.addReaderGroups(group);
 	getAllowedToRead().addPersistentGroups(group);
     }
 
     @Override
     public void removeReaderGroups(PersistentGroup group) {
	super.removeReaderGroups(group);
 	getAllowedToRead().removePersistentGroups(group);
     }
 
     public void generateProtocolDir() {
 	PersistentGroup fileReaderGroup = this.getVisibilityType() == ProtocolVisibilityType.TOTAL ? AnyoneGroup.getInstance()
 		: this.getAllowedToRead();
 
 	ProtocolDirNode dir = this.getProtocolDir();
 
 	if (dir != null) {
 	    dir.setName(this.getProtocolNumber());
 	    dir.setReadGroup(fileReaderGroup);
 	    dir.setWriters(this.getWriterGroup());
 	} else {
 	    this.setProtocolDir(new ProtocolDirNode(this.getWriterGroup(), this.getProtocolNumber(), fileReaderGroup));
 	}
     }
 
     public boolean canBeReadByUser(final User user) {
 	return getVisibilityType() != ProtocolVisibilityType.RESTRICTED || getAllowedToRead().isMember(user)
 		|| canBeWrittenByUser(user);
     }
 
     public boolean canFilesBeReadByUser(User user) {
 
 	return getVisibilityType() == ProtocolVisibilityType.TOTAL || getAllowedToRead().isMember(user)
 		|| canBeWrittenByUser(user);
     }
 
     public boolean canBeWrittenByUser(final User user) {
 	return getWriterGroup().getAuthorizedWriterGroup().isMember(user)
 		|| ProtocolManager.getInstance().getAdministrativeGroup().isMember(user);
     }
 
     public String getPartners() {
 	StringBuilder builder = new StringBuilder();
 	boolean first = true;
 	for (ProtocolResponsible responsible : getProtocolResponsible()) {
 	    if (responsible.getType() == ProtocolResponsibleType.INTERNAL)
 		continue;
 
 	    if (first)
 		first = false;
 	    else
 		builder.append(", ");
 
 	    builder.append(responsible.getUnit().getPartyName().getContent());
 	}
 	return builder.toString();
     }
 
     public String getAllResponsibles() {
 	StringBuilder builder = new StringBuilder();
 	for (ProtocolResponsible responsible : getProtocolResponsible()) {
 
 	    String responsibleStr = responsible.getPresentationString();
 
 	    if (builder.length() > 0 && !responsibleStr.isEmpty())
 		builder.append(", ");
 
 	    builder.append(responsible.getPresentationString());
 
 	}
 	return builder.toString();
     }
 
     public List<FileNode> getFiles() {
 	return getFilesFromDir(getProtocolDir());
     }
 
     private List<FileNode> getFilesFromDir(DirNode node) {
 	List<FileNode> files = new ArrayList<FileNode>();
 
 	for (AbstractFileNode file : node.getChild()) {
 	    if (file instanceof FileNode) {
 		FileNode fileNode = (FileNode) file;
 		files.add(fileNode);
 	    } else if (file instanceof DirNode) {
 		files.addAll(getFilesFromDir((DirNode) file));
 	    }
 	}
 
 	return files;
     }
 
     @Service
     public void uploadFile(String filename, byte[] contents) {
 
 	Document document = new Document(filename, filename, contents);
 
 	new FileNode(getProtocolDir(), document);
 
     }
 
     public boolean isNational() {
 	for (ProtocolResponsible responsible : getProtocolResponsible()) {
 	    if (responsible.getType() == ProtocolResponsibleType.EXTERNAL && responsible.getCountry() != null
 		    && responsible.getCountry().equals(Country.getPortugal()))
 		return true;
 	}
 	return false;
     }
 
     public boolean isInternational() {
 	for (ProtocolResponsible responsible : getProtocolResponsible()) {
 	    if (responsible.getType() == ProtocolResponsibleType.EXTERNAL && responsible.getCountry() != null
 		    && !responsible.getCountry().equals(Country.getPortugal()))
 		return true;
 	}
 	return false;
     }
 
     public boolean hasNationality() {
 	for (ProtocolResponsible responsible : getProtocolResponsible()) {
 	    if (responsible.getType() == ProtocolResponsibleType.EXTERNAL && responsible.getCountry() != null)
 		return true;
 	}
 	return false;
     }
 
     public boolean hasPartnerInCountry(Country country) {
 
 	if (country == null)
 	    return true;
 
 	for (ProtocolResponsible responsible : getProtocolResponsible()) {
 	    if (responsible.getType() == ProtocolResponsibleType.EXTERNAL && country.equals(responsible.getCountry()))
 		return true;
 	}
 	return false;
     }
 
     public String getVisibilityDescription() {
 	switch (getVisibilityType()) {
 	case PROTOCOL:
 	    return BundleUtil.getFormattedStringFromResourceBundle("resources/ProtocolsResources",
 		    "label.protocols.visibility.protocol", generateVisibilityString());
 	case RESTRICTED:
 	    return BundleUtil.getFormattedStringFromResourceBundle("resources/ProtocolsResources",
 		    "label.protocols.visibility.restricted", generateVisibilityString());
 	case TOTAL:
 	    return BundleUtil.getStringFromResourceBundle("resources/ProtocolsResources", "label.protocols.visibility.total");
 	default:
 	    return "";
 	}
     }
 
     private String generateVisibilityString() {
 	Set<PersistentGroup> groups = new HashSet<PersistentGroup>();
 
 	groups.add(getWriterGroup().getAuthorizedWriterGroup());
 
 	for (PersistentGroup group : getReaderGroups()) {
 	    groups.add(group);
 	}
 
 	StringBuilder builder = new StringBuilder(groups.size());
 
 	for (PersistentGroup group : groups) {
 	    if (builder.length() > 0)
 		builder.append(", ");
 	    builder.append(group.getName());
 	}
 
 	return builder.toString();
     }
 }
