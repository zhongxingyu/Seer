 package com.jtbdevelopment.e_eye_o.DAO.helpers;
 
 import com.jtbdevelopment.e_eye_o.DAO.ReadWriteDAO;
 import com.jtbdevelopment.e_eye_o.entities.AppUser;
 import com.jtbdevelopment.e_eye_o.entities.AppUserSettings;
 import com.jtbdevelopment.e_eye_o.entities.IdObjectFactory;
 import com.jtbdevelopment.e_eye_o.entities.TwoPhaseActivity;
 import com.jtbdevelopment.e_eye_o.helpandlegal.CookiesPolicy;
 import com.jtbdevelopment.e_eye_o.helpandlegal.PrivacyPolicy;
 import com.jtbdevelopment.e_eye_o.helpandlegal.TermsAndConditions;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.crypto.password.PasswordEncoder;
 
 /**
  * Date: 4/6/13
  * Time: 6:26 PM
  * <p/>
  * A base implementation you can extend for new user setup
  * You should implement a createSamplesForNewUser routine
  * to create default entries (if any) for new users
  */
 public abstract class AbstractUserHelperImpl implements UserHelper {
    @Autowired
     private TermsAndConditions termsAndConditions;
 
    @Autowired
     private PrivacyPolicy privacyPolicy;
 
    @Autowired
     private CookiesPolicy cookiesPolicy;
 
     @Autowired
     protected ReadWriteDAO readWriteDAO;
 
     @Autowired
     protected IdObjectFactory idObjectFactory;
 
     @Autowired(required = false)
     protected PasswordEncoder passwordEncoder;
 
     protected abstract void createSamplesForNewUser(final AppUser newUser);
 
     @Override
     public TwoPhaseActivity setUpNewUser(final AppUser appUser) {
         appUser.setPassword(securePassword(appUser.getPassword()));
         AppUser savedUser = readWriteDAO.create(appUser);
         createDefaultSettingsForNewUser(savedUser);
         createSamplesForNewUser(savedUser);
         return generateActivationRequest(savedUser);
     }
 
     @Override
     public AppUserSettings createDefaultSettingsForNewUser(final AppUser newUser) {
         return readWriteDAO.create(
                 idObjectFactory.newAppUserSettingsBuilder(newUser)
                        .withSetting(AppUserSettings.COOKIES_POLICY_VERSION, cookiesPolicy.getVersion())
                        .withSetting(AppUserSettings.PRIVACY_POLICY_VERSION, privacyPolicy.getVersion())
                        .withSetting(AppUserSettings.TERMS_AND_CONDITIONS_VERSION, termsAndConditions.getVersion())
                         .build()
         );
     }
 
     @Override
     public TwoPhaseActivity generateActivationRequest(final AppUser appUser) {
         return readWriteDAO.create(idObjectFactory.newTwoPhaseActivityBuilder(appUser).withActivityType(TwoPhaseActivity.Activity.ACCOUNT_ACTIVATION).withExpirationTime(new DateTime().plusDays(1)).build());
     }
 
     private String securePassword(final String clearCasePassword) {
         if (passwordEncoder != null) {
             return passwordEncoder.encode(clearCasePassword);
         }
         return clearCasePassword;
     }
 
     @Override
     public TwoPhaseActivity requestResetPassword(AppUser appUser) {
         TwoPhaseActivity requestReset = idObjectFactory.newTwoPhaseActivityBuilder(appUser).withActivityType(TwoPhaseActivity.Activity.PASSWORD_RESET).withExpirationTime(new DateTime().plusDays(1)).build();
         requestReset = readWriteDAO.create(requestReset);
         return requestReset;
     }
 
     @Override
     public void resetPassword(final TwoPhaseActivity twoPhaseActivity, final String newPassword) {
         readWriteDAO.resetUserPassword(twoPhaseActivity, securePassword(newPassword));
     }
 
 }
