 package com.adito.radius;
 
 import com.adito.security.AuthenticationScheme;
 import com.adito.security.Credentials;
 import com.adito.security.PasswordCredentials;
 import com.maverick.crypto.encoders.Base64;
 import com.adito.core.RequestParameterMap;
 import com.adito.core.UserDatabaseManager;
 import com.adito.core.stringreplacement.VariableReplacement;
 import com.adito.properties.Property;
 import com.adito.properties.impl.realms.RealmKey;
 
 import com.adito.security.*;
 import com.adito.util.Utils;
 
 import java.net.InetAddress;
 import java.util.StringTokenizer;
 import javax.servlet.http.*;
 
 import net.sf.jradius.client.RadiusClient;
 import net.sf.jradius.client.auth.PAPAuthenticator;
 import net.sf.jradius.client.auth.RadiusAuthenticator;
 import net.sf.jradius.dictionary.Attr_ReplyMessage;
 import net.sf.jradius.exception.*;
 import net.sf.jradius.packet.*;
 import net.sf.jradius.packet.attribute.AttributeFactory;
 import net.sf.jradius.packet.attribute.AttributeList;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 public class RADIUSAuthenticationModule
     implements AuthenticationModule
 {
 
     public RADIUSAuthenticationModule()
     {
         f = 1;
         m = null;
         k = null;
     }
 
     protected User doLogon(HttpServletRequest httpservletrequest, String s, String s1)
         throws UserDatabaseException, InvalidLoginCredentialsException, AccountLockedException, InputRequiredException, SecurityErrorException, RadiusException
     {
         try
         {
             String s2 = Property.getProperty(new RealmKey("radius.usernameCase", scheme.getUser().getRealm().getRealmID()));
             if(!s2.equals("none"))
                 if(s2.equals("upper"))
                     s = s.toUpperCase();
                 else
                     s = s.toLowerCase();
         }
         catch(Exception exception) { }
         if(b(s, s1))
         {
             h.debug("RADIUS Validation SUCCEEDED!");
             try
             {
                 User user = UserDatabaseManager.getInstance().getUserDatabase(scheme.getUser().getRealm().getRealmID()).getAccount(s);
                 return user;
             }
             catch(Exception exception1)
             {
                 throw new UserDatabaseException("Failed to get user account.", exception1);
             }
         } else
         {
             h.warn("RADIUS Validation FAILED!");
             throw new InvalidLoginCredentialsException();
         }
     }
 
     private void b(AttributeList attributelist, String s, String s1)
         throws RadiusException
     {
         String s2;
         for(StringTokenizer stringtokenizer = new StringTokenizer(i, "!"); stringtokenizer.hasMoreElements(); attributelist.add(AttributeFactory.attributeFromString(s2), false))
         {
             s2 = (String)stringtokenizer.nextElement();
             s2 = Utils.replace(s2, "%USERNAME%", s);
             s2 = Utils.replace(s2, "%PASSWORD%", s1);
             VariableReplacement variablereplacement = new VariableReplacement();
             variablereplacement.setServletRequest(e);
             s2 = variablereplacement.replace(s2);
         }
 
     }
 
     private boolean b(String s, String s1)
         throws InputRequiredException, SecurityErrorException, RadiusException
     {
         RadiusClient radiusclient;
         radiusclient = null;
 
         boolean flag1;        
         
         try
         {
             radiusclient = new RadiusClient(d, g, n, l, j);
         }
         catch(Throwable throwable)
         {
             if(h.isErrorEnabled())
                 h.error("Failed to construct RadiusClient.", throwable);
             throw new SecurityErrorException(15001, "radius", throwable, null);
         }
         
         //boolean flag = false;
         
         RadiusAuthenticator radiusauthenticator;
         AccessRequest accessrequest;
         if(h.isInfoEnabled())
             h.info("Entering RADIUS Validation...");
         AttributeList attributelist = new AttributeList();
         radiusauthenticator = null;
         if("true".equalsIgnoreCase(e.getParameter("hasChallenges")))
         {
             attributelist.add(AttributeFactory.attributeFromString((new StringBuilder()).append("User-Name = ").append(scheme.getUsername()).toString()), false);
             attributelist.add(m.findAttribute("State"));
             for(int i1 = 0; i1 < k.length; i1++)
             {
                 Attr_ReplyMessage attr_replymessage = (Attr_ReplyMessage)k[i1];
                 attr_replymessage.setValue(e.getParameter(attr_replymessage.getValue().toString()));
                 attributelist.add(attr_replymessage);
             }
 
         } else
         {
             b(attributelist, s, s1);
         }
         accessrequest = new AccessRequest(radiusclient, attributelist);
         if((radiusauthenticator = RadiusClient.getAuthProtocol(c)) == null)
         {
             System.err.println((new StringBuilder()).append("Unsupported authentication protocol ").append(c).toString());
             return false;
         }
        RadiusResponse radiusresponse;
         if(h.isDebugEnabled())
             h.debug((new StringBuilder()).append("Sending:\n").append(accessrequest.toString()).toString());
         radiusresponse = b(radiusclient, (AccessRequest)accessrequest, radiusauthenticator, b, !"true".equalsIgnoreCase(e.getParameter("hasChallenges")));
         if(radiusresponse == null)
             return false;
 
         if(h.isDebugEnabled())
 		    h.debug((new StringBuilder()).append("Received:\n").append(radiusresponse.toString()).toString());
 		
         if(radiusresponse instanceof AccessAccept)
 		{
 		    flag1 = true;
 		} 
         else
 		{
 		    if(radiusresponse instanceof AccessChallenge)
 		    {
 		        m = (AccessChallenge)radiusresponse;
 		        k = m.findAttributes(18);
 		        e.getSession().setAttribute("radiusChallenges", ((Object) (k)));
 		        throw new InputRequiredException();
 		    }
 		    flag1 = false;
 		}
         
         return flag1;
     }
 
     private RadiusResponse b(RadiusClient radiusclient, AccessRequest accessrequest, RadiusAuthenticator radiusauthenticator, int i1, boolean flag)
         throws RadiusException, UnknownAttributeException
     {
         if(radiusauthenticator == null)
             radiusauthenticator = new PAPAuthenticator();
         if(flag)
         {
             radiusauthenticator.setupRequest(radiusclient, accessrequest);
             radiusauthenticator.processRequest(accessrequest);
         }
        return radiusclient.sendReceive(accessrequest, radiusclient.getRemoteInetAddress(), radiusclient.getAuthPort(), i1);
     }
 
     private boolean b(String s)
         throws InputRequiredException, SecurityErrorException, UnknownAttributeException, RadiusException
     {
         RadiusClient radiusclient;
         radiusclient = null;
         try
         {
             radiusclient = new RadiusClient(d, g, n, l, j);
         }
         catch(Throwable throwable)
         {
             if(h.isErrorEnabled())
                 h.error("Failed to construct RadiusClient.", throwable);
             throw new SecurityErrorException(15001, "radius", throwable, null);
         }
         //boolean flag = false;
         RadiusAuthenticator radiusauthenticator;
         AccessRequest accessrequest;
         if(h.isInfoEnabled())
             h.info("Entering RADIUS Validation...");
         AttributeList attributelist = new AttributeList();
         radiusauthenticator = null;
         attributelist.add(AttributeFactory.attributeFromString((new StringBuilder()).append("User-Name = ").append(s).toString()), false);
         accessrequest = new AccessRequest(radiusclient, attributelist);
         if((radiusauthenticator = RadiusClient.getAuthProtocol(c)) == null)
         {
             System.err.println((new StringBuilder()).append("Unsupported authentication protocol ").append(c).toString());
             return false;
         }
         RadiusResponse radiusresponse;
         if(h.isDebugEnabled())
             h.debug((new StringBuilder()).append("Sending:\n").append(accessrequest.toString()).toString());
         radiusresponse = b(radiusclient, (AccessRequest)accessrequest, radiusauthenticator, b, !"true".equalsIgnoreCase(e.getParameter("hasChallenges")));
         if(radiusresponse == null)
             return false;
         if(h.isDebugEnabled())
             h.debug((new StringBuilder()).append("Received:\n").append(radiusresponse.toString()).toString());
         if(radiusresponse instanceof AccessChallenge)
         {
             f = 2;
             m = (AccessChallenge)radiusresponse;
             k = m.findAttributes(18);
             e.getSession().setAttribute("radiusChallenges", ((Object) (k)));
             return true;
         }
         //boolean flag1;
 
         throw new SecurityErrorException(15004, "radius", null, null);
        
     }
 
     public void init(AuthenticationScheme authenticationscheme)
     {
         scheme = authenticationscheme;
         if(authenticationscheme == null)
             h.info("The scheme is NULL!!");
         else
         if(authenticationscheme.getUser() == null)
             h.info("The user is NULL!!");
         try
         {
             d = InetAddress.getByName(Property.getProperty(new RealmKey("radius.serverHostName", authenticationscheme.getUser().getRealm().getRealmID())).toLowerCase().trim());
             if(h.isDebugEnabled())
                 h.debug((new StringBuilder()).append("Setting RADIUS server to: ").append(d).toString());
             n = Integer.parseInt(Property.getProperty(new RealmKey("radius.authenticationPort", authenticationscheme.getUser().getRealm().getRealmID())));
             if(h.isDebugEnabled())
                 h.debug((new StringBuilder()).append("Setting RADIUS authentication port to: ").append(n).toString());
             l = Integer.parseInt(Property.getProperty(new RealmKey("radius.accountingPort", authenticationscheme.getUser().getRealm().getRealmID())));
             if(h.isDebugEnabled())
                 h.debug((new StringBuilder()).append("Setting RADIUS accounting port to: ").append(l).toString());
             j = Integer.parseInt(Property.getProperty(new RealmKey("radius.timeOut", authenticationscheme.getUser().getRealm().getRealmID())));
             if(h.isDebugEnabled())
                 h.debug((new StringBuilder()).append("Setting RADIUS time out to: ").append(j).toString());
             b = Integer.parseInt(Property.getProperty(new RealmKey("radius.retries", authenticationscheme.getUser().getRealm().getRealmID())));
             if(h.isDebugEnabled())
                 h.debug((new StringBuilder()).append("Setting RADIUS retries to: ").append(b).toString());
             g = Property.getProperty(new RealmKey("radius.sharedSecret", authenticationscheme.getUser().getRealm().getRealmID()));
             if(h.isDebugEnabled())
                 h.debug("Setting RADIUS shared secret!");
             c = Property.getProperty(new RealmKey("radius.authenticationMethod", authenticationscheme.getUser().getRealm().getRealmID()));
             if(h.isDebugEnabled())
                 h.debug((new StringBuilder()).append("Setting RADIUS authentication method to: ").append(c).toString());
             i = Property.getProperty(new RealmKey("radius.attributes", authenticationscheme.getUser().getRealm().getRealmID()));
             if(h.isDebugEnabled())
                 h.debug((new StringBuilder()).append("Setting RADIUS attributes to: ").append(i).toString());
         }
         catch(Exception exception)
         {
             h.error("Failed to get RADIUS configuration. Authentication will fail.", exception);
         }
     }
 
     public void authenticationComplete()
     {
     }
 
     public String getInclude()
     {
         return "/WEB-INF/jsp/radiusAuth.jspf";
     }
 
     public String getName()
     {
         return "RADIUS";
     }
 
     public boolean isRequired()
     {
         return true;
     }
 
     public ActionForward startAuthentication(ActionMapping actionmapping, HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse)
         throws SecurityErrorException
     {
         e = httpservletrequest;
         try
         {
             if(Property.getPropertyBoolean(new RealmKey("radius.expectChallenge", scheme.getUser().getRealm().getRealmID())))
             {
                 b(scheme.getUsername());
                 return actionmapping.findForward("display");
             }
         }
         catch(Exception exception)
         {
             exception.printStackTrace();
             h.error(exception);
             return actionmapping.findForward("display");
         }
         return actionmapping.findForward("display");
     }
 
     public Credentials authenticate(HttpServletRequest httpservletrequest, RequestParameterMap requestparametermap)
         throws InvalidLoginCredentialsException, AccountLockedException, SecurityErrorException, InputRequiredException
     {
         e = httpservletrequest;
         if(scheme.getUser() == null)
         {
             String s = requestparametermap.getParameter("username");
             if(s == null || s.equals(""))
                 throw new InvalidLoginCredentialsException();
             try
             {
                 scheme.setUser(UserDatabaseManager.getInstance().getUserDatabase(scheme.getUser().getRealm().getRealmID()).getAccount(s));
             }
             catch(Exception exception)
             {
                 throw new InvalidLoginCredentialsException("Failed to load user.", exception);
             }
             scheme.setAccountLock(LogonControllerFactory.getInstance().checkForAccountLock(s, ""));
         }
         try
         {
             String s1 = null;
             if(httpservletrequest.getHeader("Authorization") != null)
             {
                 String s2 = httpservletrequest.getHeader("Authorization");
                 int i1 = s2.indexOf(' ');
                 if(i1 > 0 && i1 <= s2.length() - 1)
                 {
                     String s3 = s2.substring(0, i1);
                     if(s3.equalsIgnoreCase("basic"))
                     {
                         if(h.isDebugEnabled())
                             h.debug("Using BASIC authentication");
                         String s4 = s2.substring(i1 + 1);
                         String s5 = new String(Base64.decode(s4));
                         i1 = s5.indexOf(':');
                         if(i1 > -1)
                         {
                             String s6 = s5.substring(0, i1);
                             s1 = s5.substring(i1 + 1);
                             if(!s6.equalsIgnoreCase(scheme.getUsername()))
                                 throw new InvalidLoginCredentialsException("Internal Error");
                         }
                     }
                 }
             }
             if(s1 == null)
                 s1 = requestparametermap.getParameter("password");
             try
             {
                 User user = doLogon(httpservletrequest, scheme.getUsername(), s1);
                 if(scheme.getUser() == null && user != null)
                     scheme.setUser(user);
             }
             catch(UserDatabaseException userdatabaseexception)
             {
                 throw new SecurityErrorException(15003, "radius", userdatabaseexception, userdatabaseexception.getMessage());
             } catch (RadiusException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
             credentials = new PasswordCredentials(scheme.getUsername(), s1.toCharArray());
             return credentials;
         }
         catch(InvalidLoginCredentialsException invalidlogincredentialsexception)
         {
             throw invalidlogincredentialsexception;
         }
     }
 
     static final Log h = LogFactory.getLog(RADIUSAuthenticationModule.class);
     private InetAddress d;
     private int n;
     private int l;
     private int j;
     private int b;
     private String c;
     private String g;
     private String i;
     protected AuthenticationScheme scheme;
     protected PasswordCredentials credentials;
     protected String moduleName;
     protected boolean required;
     private HttpServletRequest e;
     public static final String MODULE_NAME = "RADIUS";
     public static final int INITIAL_STATE = 1;
     public static final int CHALLENGE_STATE = 2;
     int f;
     AccessChallenge m;
     Object k[];
 
 }
