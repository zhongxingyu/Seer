 package org.vamdc.portal.session.queryBuilder.forms;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import org.vamdc.dictionary.Restrictable;
 import org.vamdc.dictionary.VSSPrefix;
 import org.vamdc.portal.session.queryBuilder.QueryData;
 import org.vamdc.portal.session.queryBuilder.fields.AbstractField;
 import org.vamdc.tapservice.vss2.LogicNode;
 import org.vamdc.tapservice.vss2.NodeFilter;
 import org.vamdc.tapservice.vss2.Prefix;
 import org.vamdc.tapservice.vss2.RestrictExpression;
 
 public abstract class AbstractForm implements Form{
 
 
 	private static final long serialVersionUID = -2420596698571830221L;
 	protected List<AbstractField> fields;
 	private Set<Restrictable> supportedKeywords;
 	protected transient QueryData queryData;
 	private final String id;
 	protected String prefix;
 	private Integer insertOrder;
 
 
 	public AbstractForm(){ 
 		this.id=UUID.randomUUID().toString();
 		this.prefix="";
 		this.fields = new ArrayList<AbstractField>();
 		this.supportedKeywords = EnumSet.noneOf(Restrictable.class);
 	}
 
 	@Override
 	public void setQueryData(QueryData queryData){ 
 		this.queryData = queryData;
 	}
 
 	void addField(AbstractField field){
 		fields.add(field);
 		if (field.getKeyword()!=null)
 			supportedKeywords.add(field.getKeyword());
 	}
 
 	@Override
 	public String getQueryPart() {
 		String query="";
 		for (AbstractField field:fields){
 			if (field.hasValue()){
 				if (query.length()>0){
 					query+=" AND "+field.getQuery();
 				}
 				else query=field.getQuery();
 			}
 		}
 		return query;
 	}
 
 	@Override
 	public Collection<AbstractField> getFields() { return fields; }
 
 	@Override
 	public Collection<Restrictable> getKeywords() {
 		EnumSet<Restrictable> keywords = EnumSet.noneOf(Restrictable.class);
 		for (AbstractField field:fields){
			if (field.hasValue() && field.getKeyword()!=null){
 				keywords.add(field.getKeyword());
 			}
 		}
 		return keywords;
 	}
 
 	@Override
 	public Collection<Restrictable> getSupportedKeywords(){
 		return supportedKeywords;
 	}
 
 	@Override
 	public void clear(){
 		for (AbstractField field:fields){
 			field.clear();
 		}
 	}
 
 	@Override
 	public void delete(){
 		clear();
 		queryData.deleteForm(this);
 	}
 
 	@Override
 	public String getId(){ return this.id; }
 
 	@Override
 	public Integer getInsertOrder() { return insertOrder; }
 
 	@Override
 	public void setInsertOrder(Integer insertOrder) { this.insertOrder = insertOrder; }
 
 	@Override
 	public void setPrefix(String prefix){
 		if (prefix==null)
 			prefix="";
 		this.prefix = prefix;
 
 		for (AbstractField field:fields){
 			field.setPrefix(prefix);
 		}
 	}
 	@Override
 	public void setPrefixIndex(Integer index){
 		String prefix = this.prefix;
 		if (index!=null && prefix!=null && prefix.length()>0){
 			prefix=prefix+index;
 			for (AbstractField field:fields){
 				field.setPrefix(prefix);
 			}
 		}
 	}
 
 	@Override
 	public String getPrefix(){ return this.prefix; }
 
 	@Override
 	public int loadFromQuery(LogicNode branch){
 		int loadedFieldsCount=0;
 		for (AbstractField field:fields){
 			if (field.getKeyword()!=null){
 				LogicNode part = NodeFilter.filterKeywords(branch, EnumSet.of(field.getKeyword()));
 				part = filterByFieldPrefix(field, part);
 				if (part!=null){
 					field.loadFromQuery(part);
 					loadedFieldsCount++;
 				}
 			}
 		}
 		
 		loadPrefix(branch, loadedFieldsCount);
 		
 		return loadedFieldsCount;
 	}
 
 	private void loadPrefix(LogicNode branch, int loadedFieldsCount) {
 		if (loadedFieldsCount>0 && this.getOrder() <= Order.SPECIES_LIMIT){
 			String prefix = extractPrefix(branch);
 			if (prefix.length()>0)
 				this.setPrefix(prefix);
 		}
 	}
 
 	private LogicNode filterByFieldPrefix(AbstractField field, LogicNode part) {
 		String prefix = field.getPrefix();
 		if (part!=null && prefix!=null && prefix.length()>0){
 			part = NodeFilter.filterPrefix(part, new Prefix(VSSPrefix.valueOf(prefix.toUpperCase()), 0));
 		}
 		return part;
 	}
 
 	private String extractPrefix(LogicNode branch){
 		Prefix pref = findRestrictExpression(branch).getPrefix();
 		if (pref!=null && pref.getPrefix()!=null)
 			return pref.getPrefix().name().toLowerCase();
 		return "";
 	}
 
 	private RestrictExpression findRestrictExpression(LogicNode branch){
 		if (branch instanceof RestrictExpression)
 			return (RestrictExpression)branch;
 		else{
 			Object node = branch.getValue();
 			if (node instanceof RestrictExpression)
 				return (RestrictExpression)node;
 			else 
 				return findRestrictExpression((LogicNode) node);
 
 
 		}
 	}
 
 }
