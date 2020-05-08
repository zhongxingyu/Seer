 /*
  * Copyright (c) 2006-2012 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.dialogs.profiles;
 
 import com.dmdirc.config.Identity;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.util.validators.FileNameValidator;
 import com.dmdirc.util.validators.IdentValidator;
 import com.dmdirc.util.validators.ValidationResponse;
 
 import com.google.common.collect.ImmutableList;
 
 import com.palantir.ptoss.cinch.core.DefaultBindableModel;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Model used to store state for the profile manager dialog.
  */
 public class ProfileManagerModel extends DefaultBindableModel {
 
     /** List of known profiles. */
     private List<Profile> profiles = new ArrayList<Profile>();
     /** List of profiles to be displayed. */
     private List<Profile> displayedProfiles = new ArrayList<Profile>();
     /** Selected profile. */
     private Profile selectedProfile;
     /** Selected nickname. */
     private String selectedNickname;
 
     /**
      * Creates a new model.
      *
      * @param identityManager Identity manager to retrieve profiles from
      */
     public ProfileManagerModel(final IdentityManager identityManager) {
         final List<Identity> identities = identityManager
                 .getIdentitiesByType("profile");
         for (Identity identity : identities) {
             profiles.add(new Profile(identity));
         }
         updateDisplayedProfiles();
         if (!profiles.isEmpty()) {
             selectedProfile = profiles.get(0);
         }
     }
 
     /**
      * Updates the list of displayed profiles, showing only those not marked
      * deleted.
      */
     private void updateDisplayedProfiles() {
         displayedProfiles.clear();
         for (Profile profile : profiles) {
             if (!profile.isDeleted()) {
                 displayedProfiles.add(profile);
             }
         }
     }
 
     /**
      * Gets the list of displayable profiles.
      *
      * @return List of profiles to display
      */
     public List<Profile> getProfiles() {
         return ImmutableList.copyOf(displayedProfiles);
     }
 
     /**
      * Sets the list of profiles.
      *
      * @param profiles List of profiles to display
      */
     public void setProfiles(final List<Profile> profiles) {
         this.profiles = new ArrayList<Profile>(profiles);
         updateDisplayedProfiles();
         if (!profiles.contains(selectedProfile)) {
             upadateSelectedProfile(null);
         }
         update();
         if (selectedProfile == null && !profiles.isEmpty()) {
             upadateSelectedProfile(profiles.get(0));
         }
         update();
     }
 
     /**
      * Adds the specified profile to the list.
      *
      * @param profile New profile
      */
     public void addProfile(final Profile profile) {
         profiles.add(profile);
         updateDisplayedProfiles();
         update();
         upadateSelectedProfile(profile);
         update();
     }
 
     /**
      * Marks the selected profile as deleted.
      *
      * @param profile Profile to delete
      */
     public void deleteProfile(final Profile profile) {
         profile.setDeleted(true);
         final int selected;
         if (selectedProfile == null) {
             selected = -1;
         } else {
             selected = displayedProfiles.indexOf(selectedProfile);
         }
         displayedProfiles.remove(profile);
         final int size = displayedProfiles.size();
         Profile newSelectedProfile = null;
         if (profile != selectedProfile) {
             newSelectedProfile = selectedProfile;
         } else if (selected >= size && size != 0) {
             newSelectedProfile = displayedProfiles.get(size - 1);
         } else if (selected <= 0 && size != 0) {
             newSelectedProfile = displayedProfiles.get(0);
         }
         update();
         upadateSelectedProfile(newSelectedProfile);
         update();
     }
 
     /**
      * Updates the selected profile.  This method clears the selected nickname,
      * the update method is not called by this method.
      *
      * @param profile Newly selected profile, may be null
      */
     private void upadateSelectedProfile(final Profile profile) {
         selectedProfile = profile;
         selectedNickname = null;
     }
 
     /**
      * Sets the selected profile.
      *
      * @param selectedProfile Profile to select, null ignored
      */
     public void setSelectedProfile(final Object selectedProfile) {
         if (selectedProfile != null
                 && !profiles.isEmpty()
                 && profiles.contains((Profile) selectedProfile)) {
             upadateSelectedProfile((Profile) selectedProfile);
         }
         update();
     }
 
     /**
      * Retrieves the selected profile.
      *
      * @return Selected profile (String)
      */
     public Object getSelectedProfile() {
         return selectedProfile;
     }
 
     /**
      * Retrieves the list of nicknames for the active profile, this will return
      * an empty list if there is no selected profile.
      *
      * @return List of nicknames
      */
     public List<String> getNicknames() {
         if (selectedProfile == null) {
             return Collections.emptyList();
         }
         return selectedProfile.getNicknames();
     }
 
     /**
      * Sets the list of nicknames for the active profile.  Will do nothing if
      * there is no active profile.
      *
      * @param nicknames List of nicknames
      */
     public void setNicknames(final List<String> nicknames) {
         if (selectedProfile == null) {
             return;
         }
         selectedProfile.setNicknames(nicknames);
         update();
     }
 
     /**
      * Adds the specified nickname to the list of nicknames in the active
      * profile.  Will do nothing if there is no active profile.
      *
      * @param nickname New nickname
      */
     public void addNickname(final String nickname) {
         if (selectedProfile == null) {
             return;
         }
         selectedProfile.addNickname(nickname);
         update();
         selectedNickname = nickname;
         update();
     }
 
     /**
      * Deletes the specified nickname from the active profile. This method will
      * do nothing if there is no active profile.
      *
      * @param nickname Nickname to be deleted (This method will do nothing if
      * this is not a String)
      */
     public void deleteNickname(final Object nickname) {
         if (nickname instanceof String) {
             deleteNickname((String) nickname);
         }
     }
 
     /**
      * Deletes the specified nickname from the active profile. This method
      * will do nothing if there is no active profile.
      *
      * @param nickname Nickname to be deleted
      */
     public void deleteNickname(final String nickname) {
         if (selectedProfile == null || nickname == null) {
             return;
         }
         final int selected = selectedProfile.getNicknames().indexOf(selectedProfile);
         selectedProfile.delNickname(nickname);
         final int size = selectedProfile.getNicknames().size();
         String newSelectedNickname = null;
         if (nickname.equals(selectedNickname)) {
             newSelectedNickname = selectedNickname;
         } else if (selected >= size && size != 0) {
             newSelectedNickname = selectedProfile.getNicknames().get(size - 1);
         } else if (selected <= 0 && size != 0) {
             newSelectedNickname = selectedProfile.getNicknames().get(0);
         }
         update();
         selectedNickname = newSelectedNickname;
         update();
     }
 
     /**
      * Alters the specified nickname.
      *
      * @param nickname Nickname to be edited
      * @param edited Resultant nickname
      */
     public void editNickname(final String nickname, final String edited) {
         selectedNickname = edited;
         selectedProfile.editNickname(nickname, edited);
         update();
         selectedNickname = edited;
         update();
     }
 
     /**
      * Sets the selected nickname on the active profile.  This method expects a
      * String, it will do nothing if the parameter is not.  If the specified
      * nickname is not found the selection will be cleared.
      *
      * @param selectedNickname Nickname to be selected, may be null.  (This
      * method will do nothing if this is not a String)
      */
     public void setSelectedNickname(final Object selectedNickname) {
         if (selectedProfile != null
                 && selectedNickname instanceof String
                 && selectedProfile.getNicknames().contains((String) selectedNickname)) {
             this.selectedNickname = (String) selectedNickname;
             update();
         }
     }
 
     /**
      * Retrieves the selected nickname from the active profile.
      *
      * @return Selected nickname (String) or an empty string if there is no
      * active profile
      */
     public Object getSelectedNickname() {
         if (selectedProfile == null) {
             return "";
         }
         return selectedNickname;
     }
 
     /**
      * Retrieves the name of the active profile.
      *
      * @return Active profile name or an empty string if there is no
      * active profile
      */
     public String getName() {
         if (selectedProfile == null) {
             return "";
         }
         return selectedProfile.getName();
     }
 
     /**
      * Sets the name of the active profile.  This method will do nothing if
      * there is no active profile.
      *
      * @param name New profile name
      */
     public void setName(final String name) {
         if (selectedProfile != null) {
             selectedProfile.setName(name);
             update();
         }
     }
 
     /**
      * Retrieves the realname in the active profile.
      *
      * @return Active profile realname or an empty string if there is no
      * active profile
      */
     public String getRealname() {
         if (selectedProfile == null) {
             return "";
         }
         return selectedProfile.getRealname();
     }
 
     /**
      * Sets the realname in the active profile.  This method will do nothing if
      * there is no active profile.
      *
      * @param realname New profile real name
      */
     public void setRealname(final String realname) {
         if (selectedProfile != null) {
             selectedProfile.setRealname(realname);
             update();
         }
     }
 
     /**
      * Retrieves the ident of the active profile.  This method will return an
      * empty string if there is no active profile.
      *
      * @return Active profile ident or an empty string if there is no
      * active profile
      */
     public String getIdent() {
         if (selectedProfile == null) {
             return "";
         }
         return selectedProfile.getIdent();
     }
 
     /**
      * Sets the ident of the active profile. This method will do nothing if
      * there is no active profile.
      *
      * @param ident New profile ident
      */
     public void setIdent(final String ident) {
         if (selectedProfile != null) {
             selectedProfile.setIdent(ident);
             update();
         }
     }
 
     /**
      * Checks whether is it possible to manipulate a profile.
      *
      * @return true when a profile is selected
      */
     public boolean isManipulateProfileAllowed() {
         return getSelectedProfile() != null;
     }
 
     /**
      * Checks whether is it possible to manipulate a nickname.
      *
      * @return true when a nickname is selected
      */
     public boolean isManipulateNicknameAllowed() {
         return getSelectedProfile() != null && getSelectedNickname() != null;
     }
 
     /**
      * Is it possible to save and close the dialog?
      *
      * @return true if all other validators pass and there is at least one
      * profile
      */
     public boolean isOKAllowed() {
         return !profiles.isEmpty()
                 && !isNameValid().isFailure()
                 && !isNicknamesValid().isFailure()
                 && !isIdentValid().isFailure()
                 && !isRealnameValid().isFailure();
     }
 
     public ValidationResponse isNameValid() {
         if (selectedProfile == null) {
             return new ValidationResponse();
         }
         final ValidationResponse filenameValidation = new FileNameValidator()
                 .validate(selectedProfile.getName());
         if (filenameValidation.isFailure()) {
             return filenameValidation;
         }
         if (((Profile) getSelectedProfile()).getName().equals(selectedProfile.getName())) {
             return new ValidationResponse();
         }
         return new ProfileNameValidator(profiles).validate(selectedProfile.getName());
     }
 
     /**
      * Are the nicknames in the active profile valid?  If there is no active
      * profile the validation passes.
      *
      * @return passes when there are nicknames present
      */
     public ValidationResponse isNicknamesValid() {
         if (selectedProfile == null) {
             return new ValidationResponse();
         }
         if (selectedProfile.getNicknames().isEmpty()) {
             return new ValidationResponse("Nickname cannot be empty");
         }
         return new ValidationResponse();
     }
 
     /**
      * Is the realname in the active profile valid?  If there is no active
      * profile the validation passes.
      *
      * @return passes the realname is valid
      */
     public ValidationResponse isRealnameValid() {
         if (selectedProfile == null) {
             return new ValidationResponse();
         }
         if (selectedProfile.getRealname().isEmpty()) {
             return new ValidationResponse("Realname cannot be empty");
         }
         return new ValidationResponse();
     }
 
     /**
      * Is the ident in the active profile valid?  If there is no active
      * profile the validation passes.
      *
      * @return passes the ident is valid
      */
     public ValidationResponse isIdentValid() {
         if (selectedProfile == null) {
             return new ValidationResponse();
         }
        if (selectedProfile.getIdent().isEmpty()) {
             return new ValidationResponse();
         }
         return new IdentValidator().validate(selectedProfile.getIdent());
     }
 
     /**
      * This method saves any changes made in the model to disk.  All profiles
      * marked for deletion are removed and then all remaining profiles are have
      * their save method called.
      */
     public void save() {
         for (Profile profile : profiles) {
             if (profile.isDeleted()) {
                 profile.delete();
             }
         }
         for (Profile profile : profiles) {
             if (!profile.isDeleted()) {
                 profile.save();
             }
         }
     }
 }
