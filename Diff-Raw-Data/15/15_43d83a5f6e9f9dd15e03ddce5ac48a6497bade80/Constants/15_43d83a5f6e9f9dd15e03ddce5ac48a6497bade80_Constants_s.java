 package org.cotrix.domain.utils;
 
 import static java.text.DateFormat.*;
 import static org.cotrix.domain.dsl.Codes.*;
 
 import java.util.Calendar;
 
 import javax.enterprise.event.Observes;
 import javax.xml.namespace.QName;
 
 import org.cotrix.common.cdi.ApplicationEvents.Startup;
 import org.cotrix.common.cdi.Current;
 import org.cotrix.domain.attributes.Attribute;
 import org.cotrix.domain.common.Range;
 import org.cotrix.domain.common.Ranges;
 import org.cotrix.domain.dsl.Users;
 import org.cotrix.domain.memory.AttributeMS;
 import org.cotrix.domain.user.User;
 import org.cotrix.domain.values.DefaultType;
 import org.cotrix.domain.values.ValueType;
 import org.cotrix.domain.version.Version;
 import org.jboss.weld.context.RequestContext;
 import org.jboss.weld.context.bound.Bound;
 
 public class Constants {
 
 	private static User currentUser;
 	private static RequestContext requestContext;
 	
 	public static final String NS = "http://cotrix.org";
 	
 	
 	public static final QName DESCRIPTION_TYPE = q(NS,"description");
 	public static final QName ANNOTATION_TYPE = q(NS,"annotation");
 	public static final QName NAME_TYPE = q(NS,"name");
 	public static final QName OTHER_CODE_TYPE = q(NS,"other_code");
 	public static final QName OTHER_TYPE = q(NS,"other");
 	public static final QName defaultType = DESCRIPTION_TYPE;
 	public static final QName SYSTEM_TYPE = q(NS,"system");
 	
 	public static final QName NAME = q(NS,"name");
 	
 	public static final QName CREATION_TIME = q(NS,"created");
 	public static final QName UPDATE_TIME = q(NS,"updated");
 	public static final QName UPDATED_BY = q(NS,"updatedBy");
 	
 	public static final QName PREVIOUS_VERSION = q(NS,"previous_version");
 	public static final QName PREVIOUS_VERSION_ID = q(NS,"previous_version_id");
 	public static final QName PREVIOUS_VERSION_NAME = q(NS,"previous_version_name");
 	public static final QName SUPERSIDES = q(NS,"supersides");
 	
 	public static final QName STATUS = q(NS,"status");
 	
 	public static final ValueType defaultValueType = new DefaultType();
 	public static final Range defaultRange = Ranges.arbitrarily;
 	
 	public static Attribute.State timestamp(QName name) {
 		return systemAttribute(name, time());
 	}
 	
 	public static String time() {
 		return getDateTimeInstance().format(Calendar.getInstance().getTime());
 	}
 	
 	public static Attribute.State previousName(QName name) {
 		return systemAttribute(PREVIOUS_VERSION_NAME, name.toString());
 	}
 	
 	public static Attribute.State previousId(String id) {
 		return systemAttribute(PREVIOUS_VERSION_ID,id);
 	}
 	
 	public static Attribute.State previousVersion(Version version) {
 		return systemAttribute(PREVIOUS_VERSION,version.value());
 	}
 	
 	public static Attribute status(CodeStatus status) {
 		return systemAttribute(STATUS,status.name()).entity();
 	}
 	
 	public static Attribute.State updatedBy() {
		return systemAttribute(UPDATED_BY, requestContext.isActive()?currentUser.name():Users.cotrix.name());
 	}
 	
 	public static void updatedBy(Attribute.State state) {
		state.value(currentUser.name());
 	}
 	
 	
 	
 	private static Attribute.State systemAttribute(QName name, String value) {
 		AttributeMS a = new AttributeMS();
 		a.name(name);
 		a.type(SYSTEM_TYPE);
 		a.value(value);
 		return a;
 	}
 	
 	
 	public static final String UNDEFINED_LINK_VALUE = "_!!_undefined_!!_";
 	
 	
 	public static final String NULL_STRING ="__ignore__";
 	public static final QName NULL_QNAME = q("__ignore__");
 	
 	public static final String NO_MAIL = "no@cotrix.mail";
 	
 
 	
	static void setUser(@Observes Startup startup,@Current User user, @Bound RequestContext ctx) {
 		currentUser = user;
 		requestContext=ctx;
 	}
 }
