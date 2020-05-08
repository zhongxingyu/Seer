 package cz.cvut.fit.mi_mpr_dip.admission.service.user;
 
 import java.text.Normalizer;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.regex.Pattern;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.math.NumberUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.transaction.annotation.Transactional;
 
 import cz.cvut.fit.mi_mpr_dip.admission.comparator.NaturalOrderComparator;
 import cz.cvut.fit.mi_mpr_dip.admission.dao.AdmissionDao;
 import cz.cvut.fit.mi_mpr_dip.admission.dao.UserIdentityDao;
 import cz.cvut.fit.mi_mpr_dip.admission.dao.UserRoleDao;
 import cz.cvut.fit.mi_mpr_dip.admission.dao.persistence.AdmissionUniqueConstraint;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.Admission;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.collection.UserRoles;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.user.UserIdentity;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.user.UserIdentityAuthentication;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.user.UserRole;
 import cz.cvut.fit.mi_mpr_dip.admission.service.LinkService;
 import cz.cvut.fit.mi_mpr_dip.admission.util.StringPool;
 
 @RooJavaBean
 public class UserIdentityServiceImpl implements UserIdentityService {
 
 	private static final Logger log = LoggerFactory.getLogger(UserIdentityServiceImpl.class);
 	private static final String USENAME_ORDER_PATTERN = ".*\\d+";
 
 	private String[] defaultRoles;
 
 	@Autowired
 	private AdmissionDao admissionDao;
 
 	@Autowired
 	private LinkService linkService;
 
 	@Autowired
 	private UserIdentityDao userIdentityDao;
 
 	@Autowired
 	private UserPasswordService userPasswordService;
 
 	@Autowired
 	private UserRoleDao userRoleDao;
 
 	@Autowired
 	private UserSessionService userSessionService;
 
 	@Override
 	public void addAdmissionLink(UserIdentity userIdentity) {
 		Admission admission = getAdmissionDao().getAdmission(userIdentity);
 		if (isFound(admission)) {
 			Admission admissionLink = getAdmissionLink(admission);
 			userIdentity.setAdmissionLink(admissionLink);
 		}
 	}
 
 	private boolean isFound(Admission admission) {
 		AdmissionUniqueConstraint uniqueConstraint = new AdmissionUniqueConstraint(admission);
 		return uniqueConstraint.isFound();
 	}
 
 	private Admission getAdmissionLink(Admission admission) {
 		Admission admissionLink = new Admission();
 		admissionLink.setLink(getLinkService().getAdmissionLink(admission));
 
 		return admissionLink;
 	}
 
 	@Transactional
 	@Override
 	public UserIdentity getUserIdentity(String username) {
 		UserIdentity userIdentity = getUserIdentityDao().getUserIdentity(username);
 		ensureSession(userIdentity);
 		userIdentity.persist();
 		return userIdentity;
 	}
 
 	private void ensureSession(UserIdentity userIdentity) {
 		getUserSessionService().ensureUserSession(userIdentity);
 	}
 
 	@Transactional(readOnly = true)
 	@Override
 	public void buildUserIdentity(Admission admission) {
		UserIdentity userIdentity = getUniqueUserIdentity(admission.getPerson().getLastname());
 		admission.setUserIdentity(userIdentity);
 	}
 
	private UserIdentity getUniqueUserIdentity(String lastname) {
		String normalizedLowercase = getNormalizedLastname(StringUtils.trimToEmpty(lastname)).toLowerCase();
 
 		UserIdentity userIdentity = new UserIdentity();
 		userIdentity.setAuthentication(UserIdentityAuthentication.PWD);
 		userIdentity.setUsername(findUniqueUsername(normalizedLowercase));
 		getUserPasswordService().createRandomPassword(userIdentity);
 		userIdentity.setRoles(createDefaultRoles());
 
 		log.debug("Created default UserIdentity [{}]", userIdentity);
 
 		return userIdentity;
 	}
 
 	private Set<UserRole> createDefaultRoles() {
 		Set<UserRole> userRoles = new HashSet<UserRole>();
 		for (String role : getDefaultRoles()) {
 			List<UserRole> dbUserRoles = UserRole.findUserRolesByNameEquals(role).getResultList();
 			if (isNotEmpty(dbUserRoles)) {
 				userRoles.add(dbUserRoles.get(0));
 			}
 		}
 		return userRoles;
 	}
 
 	private String getNormalizedLastname(String lastname) {
 		String normalized = Normalizer.normalize(lastname, Normalizer.Form.NFD);
 		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
 
 		return pattern.matcher(normalized).replaceAll(StringPool.BLANK);
 	}
 
 	private String findUniqueUsername(String username) {
 		String uniqueUsername = username;
 		List<UserIdentity> userIdentities = UserIdentity.findUserIdentitysByUsernameLike(username).getResultList();
 		if (isNotEmpty(userIdentities)) {
 			uniqueUsername = findUniqueUsername(userIdentities);
 		}
 		return uniqueUsername;
 	}
 
 	private String findUniqueUsername(List<UserIdentity> userIdentities) {
 		Set<String> usernames = collectUsernames(userIdentities);
 		String username = findUniqueUsername(usernames);
 		return username;
 	}
 
 	private Set<String> collectUsernames(List<UserIdentity> userIdentities) {
 		Set<String> usernames = new TreeSet<String>(new NaturalOrderComparator<String>());
 		for (UserIdentity userIdentity : userIdentities) {
 			usernames.add(userIdentity.getUsername());
 		}
 		return usernames;
 	}
 
 	private String findUniqueUsername(Set<String> usernames) {
 		String[] converted = usernames.toArray(new String[usernames.size()]);
 		String uniqueUsername = getDefaultUsername(converted, usernames.size());
 		if (uniqueUsername.matches(USENAME_ORDER_PATTERN)) {
 			for (int i = 1; i < converted.length; i++) {
 				String trimmed = trimNumeric(converted[i]);
 				String expected = trimmed + i;
 				if (!expected.equals(converted[i])) {
 					uniqueUsername = expected;
 					break;
 				}
 			}
 		}
 		return uniqueUsername;
 	}
 
 	private String getDefaultUsername(String[] converted, int size) {
 		String username;
 		String base = trimNumeric(converted[0]);
 		if (converted[0].matches(USENAME_ORDER_PATTERN)) {
 			username = base;
 		} else {
 			String last = trimNonNumeric(converted[size - 1]);
 			username = base + (NumberUtils.toInt(last) + 1);
 		}
 		return username;
 	}
 
 	private String trimNonNumeric(String username) {
 		return username.replaceFirst("\\D+", StringPool.BLANK);
 	}
 
 	private String trimNumeric(String username) {
 		return username.replaceFirst("\\d+", StringPool.BLANK);
 	}
 
 	@Transactional
 	@Override
 	public void updateUserRoles(UserIdentity userIdentity, UserRoles userRoles) {
 		Set<UserRole> roles = new HashSet<UserRole>();
 		if (isNotEmpty(userRoles.getUserRoles())) {
 			for (UserRole userRole : userRoles.getUserRoles()) {
 				UserRole dbUserRole = getUserRoleDao().getUserRole(userRole.getName());
 				if (dbUserRole != null) {
 					roles.add(dbUserRole);
 				}
 			}
 		}
 		userIdentity.setRoles(roles);
 		userIdentity.persist();
 	}
 
 	private boolean isNotEmpty(Collection<?> collection) {
 		return CollectionUtils.isNotEmpty(collection);
 	}
 
 	@Required
 	public void setDefaultRoles(String[] defaultRoles) {
 		this.defaultRoles = defaultRoles;
 	}
 
 }
