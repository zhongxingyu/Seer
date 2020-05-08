 package com.mpower.service.ldap;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import javax.naming.directory.Attribute;
 import javax.naming.directory.BasicAttribute;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.ModificationItem;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.ldap.core.DirContextOperations;
 import org.springframework.ldap.core.LdapTemplate;
 import org.springframework.stereotype.Service;
 
 import com.mpower.service.impl.SessionServiceImpl;
 
 @Service("ldapService")
 public class LdapServiceImpl implements LdapService {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
 
     private static final String LDAP_USER_PASSWORD = "userPassword";
 
     private static final String LDAP_PASSWORD_CHANGE_DATE = "passwordchangedate";
 
     private static final String LDAP_LAST_LOGIN = "lastlogin";
 
     public LdapServiceImpl() {
     }
 
     private LdapTemplate ldapTemplate;
 
     public void setLdapTemplate(LdapTemplate ldapTemplate) {
         this.ldapTemplate = ldapTemplate;
     }
 
     private String getDN() {
         String siteName = SessionServiceImpl.lookupUserSiteName();
         String userName = SessionServiceImpl.lookupUserName();
         String dn = "uid=" + userName + ", ou=users, o=" + siteName;
         logger.debug("dn = " + dn);
         return dn;
     }
 
     public void changePassword(String oldpw, String newpw) {
         ModificationItem[] modificationItemArray = new ModificationItem[2];
         String newpassword = null;
         try {
             newpassword = getSHA1(newpw);
         } catch (NoSuchAlgorithmException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         } catch (UnsupportedEncodingException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
         Attribute userPasswordAttribute = new BasicAttribute(LDAP_USER_PASSWORD, newpassword);
         ModificationItem newPassword = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, userPasswordAttribute);
         modificationItemArray[0] = newPassword;
 
        Attribute userPasswordChangedAttribute = new BasicAttribute(LDAP_PASSWORD_CHANGE_DATE, format.format(convertToUtc(null).getTime()) + "Z");
         ModificationItem newPasswordChanged = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, userPasswordChangedAttribute);
         modificationItemArray[1] = newPasswordChanged;
         ldapTemplate.modifyAttributes(getDN(), modificationItemArray);
     }
 
     public boolean isPasswordChangeRequired(int days) {
         boolean required = true;
         Calendar today = Calendar.getInstance();
         Calendar previous = getLastPasswordChange();
         if (previous != null) {
             previous.add(Calendar.DAY_OF_MONTH, days);
             if (previous.after(today)) {
                 required = false;
             }
         }
         logger.debug("isPasswordChangeRequired() = " + required);
         return required;
     }
 
     public Calendar getLastPasswordChange() {
         DirContextOperations dco = ldapTemplate.lookupContext(getDN());
         Object lastLogin = dco.getObjectAttribute(LDAP_PASSWORD_CHANGE_DATE);
         Calendar last = null;
         try {
            Date lastPwChangeDate = format.parse((String) lastLogin);
             last = new GregorianCalendar();
             last.setTimeInMillis(lastPwChangeDate.getTime());
             last = convertFromUtc(last);
         } catch (ParseException e1) {
             // the previous login couldn't be parsed so return null Calendar
         }
         logger.debug("getLastPasswordChange() = " + (last != null ? last.getTime() : "null"));
         return last;
     }
 
     public Calendar getLastLogin() {
         DirContextOperations dco = ldapTemplate.lookupContext(getDN());
         Object lastLogin = dco.getObjectAttribute(LDAP_LAST_LOGIN);
         Calendar last = null;
         try {
            Date lastLoginDate = format.parse((String) lastLogin);
             last = new GregorianCalendar();
             last.setTimeInMillis(lastLoginDate.getTime());
             last = convertFromUtc(last);
         } catch (ParseException e1) {
             // the previous login couldn't be parsed so return null Calendar
         }
         logger.debug("getLastLogin() = " + (last != null ? last.getTime() : "null"));
         return last;
     }
 
     public void setLastLogin() {
         ModificationItem[] modificationItemArray = new ModificationItem[1];
         Calendar utc = convertToUtc(null);
        String ldaputc = format.format(utc.getTime()) + "Z";
         logger.debug("setLastLogin() = " + ldaputc);
         Attribute userPasswordChangedAttribute = new BasicAttribute(LDAP_LAST_LOGIN, ldaputc);
         ModificationItem newPasswordChanged = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, userPasswordChangedAttribute);
         modificationItemArray[0] = newPasswordChanged;
         ldapTemplate.modifyAttributes(getDN(), modificationItemArray);
     }
 
     private String getSHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
         byte[] bytes = MessageDigest.getInstance("SHA-1").digest(text.getBytes());
         logger.debug("sha1 = " + bytes);
         return "{SHA}" + new String(Base64.encodeBase64(bytes));
     }
 
     private Calendar convertToUtc(Calendar in) {
         if (in == null) {
             in = Calendar.getInstance();
         }
         logger.debug("before convertToUtc() = "+in.getTime());
         in.add(Calendar.MILLISECOND, -in.get(Calendar.DST_OFFSET) - in.get(Calendar.ZONE_OFFSET));
         logger.debug("after convertToUtc() = "+in.getTime());
         return in;
     }
 
     private Calendar convertFromUtc(Calendar in) {
         if (in == null) {
             in = Calendar.getInstance();
         }
         logger.debug("before convertFromUtc() = "+in.getTime());
         in.add(Calendar.MILLISECOND, in.get(Calendar.DST_OFFSET) + in.get(Calendar.ZONE_OFFSET));
         logger.debug("after convertFromUtc() = "+in.getTime());
         return in;
     }
 }
