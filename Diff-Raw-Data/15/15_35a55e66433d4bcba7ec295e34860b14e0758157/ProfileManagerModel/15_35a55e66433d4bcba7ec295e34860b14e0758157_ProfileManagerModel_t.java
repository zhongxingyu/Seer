 /*
  * Copyright (c) 2006-2014 DMDirc Developers
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
 
 import com.dmdirc.interfaces.config.ConfigProvider;
 import com.dmdirc.interfaces.config.IdentityController;
 import com.dmdirc.interfaces.config.IdentityFactory;
 import com.dmdirc.util.validators.FileNameValidator;
 import com.dmdirc.util.validators.IdentValidator;
 import com.dmdirc.util.validators.ValidationResponse;
 
 import com.google.common.collect.ImmutableList;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeListenerProxy;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Model used to store state for the profile manager dialog.
  */
 public class ProfileManagerModel {
 
     /** Profile change support. */
     private final PropertyChangeSupport pcs;
     /** Identity Controller. */
     private final IdentityController identityController;
     /** Identity Factory. */
     private final IdentityFactory identityFactory;
     /** List of known profiles. */
     private List<Profile> profiles = new ArrayList<>();
     /** List of profiles to be displayed. */
     private final List<Profile> displayedProfiles = new ArrayList<>();
     /** Selected profile. */
     private Profile selectedProfile;
     /** Selected nickname. */
     private String selectedNickname;
 
     /**
      * Creates a new model.
      *
      * @param identityController Identity manager to retrieve profiles from.
      * @param identityFactory    Factory to use when creating new profiles.
      */
     public ProfileManagerModel(
             final IdentityController identityController,
             final IdentityFactory identityFactory) {
         pcs = new PropertyChangeSupport(this);
         this.identityController = identityController;
         this.identityFactory = identityFactory;
     }
 
     /**
      * Load model with data.
      */
     public void load() {
         final List<ConfigProvider> identities = identityController.getProvidersByType("profile");
         for (ConfigProvider identity : identities) {
             profiles.add(new Profile(identityFactory, identity));
         }
         updateDisplayedProfiles();
         if (!profiles.isEmpty()) {
             selectedProfile = profiles.get(0);
         }
         final PropertyChangeListener[] listeners = pcs.getPropertyChangeListeners();
         for (PropertyChangeListener listener : listeners) {
             if (listener instanceof PropertyChangeListenerProxy) {
                 final PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
                 pcs.firePropertyChange(proxy.getPropertyName(), null, null);
             }
         }
     }
 
     /**
      * Updates the list of displayed profiles, showing only those not marked deleted.
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
         this.profiles = new ArrayList<>(profiles);
         updateDisplayedProfiles();
         if (!profiles.contains(selectedProfile)) {
             upadateSelectedProfile(null);
         }
         if (selectedProfile == null && !profiles.isEmpty()) {
             upadateSelectedProfile(profiles.get(0));
         }
         pcs.firePropertyChange("profiles", null, null);
     }
 
     /**
      * Adds the specified profile to the list.
      *
      * @param profile New profile
      */
     public void addProfile(final Profile profile) {
         profiles.add(profile);
         updateDisplayedProfiles();
         upadateSelectedProfile(profile);
         pcs.firePropertyChange("profiles", null, null);
     }
 
     /**
      * Marks the selected profile as deleted.
      *
      * @param profile Profile to delete
      */
     public void deleteProfile(final Profile profile) {
         if (profile == null) {
             return;
         }
         profile.setDeleted(true);
         final int selected = displayedProfiles.indexOf(selectedProfile);
         displayedProfiles.remove(profile);
         final int size = displayedProfiles.size();
         Profile newSelectedProfile = null;
         if (profile != selectedProfile) {
             newSelectedProfile = selectedProfile;
         } else if (selected >= size && size > 0) {
             newSelectedProfile = displayedProfiles.get(size - 1);
         } else if (selected <= 0 && size > 0) {
             newSelectedProfile = displayedProfiles.get(0);
         } else if (selected - 1 >= 0 && size != 0) {
             newSelectedProfile = displayedProfiles.get(selected - 1);
         }
         upadateSelectedProfile(newSelectedProfile);
         pcs.firePropertyChange("profiles", null, null);
     }
 
     /**
      * Updates the selected profile. This method clears the selected nickname, the update method is
      * not called by this method.
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
         pcs.firePropertyChange("selectedprofile", null, null);
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
      * Retrieves the list of nicknames for the active profile, this will return an empty list if
      * there is no selected profile.
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
      * Sets the list of nicknames for the active profile. Will do nothing if there is no active
      * profile.
      *
      * @param nicknames List of nicknames
      */
     public void setNicknames(final List<String> nicknames) {
         if (selectedProfile == null) {
             return;
         }
         selectedProfile.setNicknames(nicknames);
         pcs.firePropertyChange("nicknames", null, null);
     }
 
     /**
      * Adds the specified nickname to the list of nicknames in the active profile. Will do nothing
      * if there is no active profile.
      *
      * @param nickname New nickname
      */
     public void addNickname(final String nickname) {
         if (selectedProfile == null) {
             return;
         }
         selectedProfile.addNickname(nickname);
         selectedNickname = nickname;
         pcs.firePropertyChange("nicknames", null, null);
     }
 
     /**
      * Deletes the specified nickname from the active profile. This method will do nothing if there
      * is no active profile.
      *
      * @param nickname Nickname to be deleted (This method will do nothing if this is not a String)
      */
     public void deleteNickname(final Object nickname) {
         if (nickname instanceof String) {
             deleteNickname((String) nickname);
         }
     }
 
     /**
      * Deletes the specified nickname from the active profile. This method will do nothing if there
      * is no active profile.
      *
      * @param nickname Nickname to be deleted
      */
     public void deleteNickname(final String nickname) {
         if (selectedProfile == null || nickname == null) {
             return;
         }
         final int selected = selectedProfile.getNicknames().indexOf(nickname);
         selectedProfile.delNickname(nickname);
         final int size = selectedProfile.getNicknames().size();
         String newSelectedNickname = null;
         if (!nickname.equals(selectedNickname)) {
             newSelectedNickname = selectedNickname;
         } else if (selected >= size && size > 0) {
             newSelectedNickname = selectedProfile.getNicknames().get(size - 1);
         } else if (selected <= 0 && size > 0) {
             newSelectedNickname = selectedProfile.getNicknames().get(0);
         } else if (selected - 1 >= 0 && size != 0) {
             newSelectedNickname = selectedProfile.getNicknames().get(selected - 1);
         }
         selectedNickname = newSelectedNickname;
         pcs.firePropertyChange("nicknames", null, null);
     }
 
     /**
      * Alters the specified nickname.
      *
      * @param nickname Nickname to be edited
      * @param edited   Resultant nickname
      */
     public void editNickname(final String nickname, final String edited) {
         selectedNickname = edited;
         selectedProfile.editNickname(nickname, edited);
         selectedNickname = edited;
         pcs.firePropertyChange("nicknames", null, null);
     }
 
     /**
      * Sets the selected nickname on the active profile. This method expects a String, it will do
      * nothing if the parameter is not. If the specified nickname is not found the selection will be
      * cleared.
      *
      * @param selectedNickname Nickname to be selected, may be null. (This method will do nothing if
      *                         this is not a String)
      */
     public void setSelectedNickname(final Object selectedNickname) {
         if (selectedProfile != null
                 && selectedNickname instanceof String
                 && selectedProfile.getNicknames().contains((String) selectedNickname)) {
             this.selectedNickname = (String) selectedNickname;
             pcs.firePropertyChange("selectednickname", null, null);
         }
     }
 
     /**
      * Retrieves the selected nickname from the active profile.
      *
      * @return Selected nickname (String) or an empty string if there is no active profile
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
      * @return Active profile name or an empty string if there is no active profile
      */
     public String getName() {
         if (selectedProfile == null) {
             return "";
         }
         return selectedProfile.getName();
     }
 
     /**
      * Sets the name of the active profile. This method will do nothing if there is no active
      * profile.
      *
      * @param name New profile name
      */
     public void setName(final String name) {
         if (selectedProfile != null) {
             selectedProfile.setName(name);
             pcs.firePropertyChange("name", null, null);
         }
     }
 
     /**
      * Retrieves the realname in the active profile.
      *
      * @return Active profile realname or an empty string if there is no active profile
      */
     public String getRealname() {
         if (selectedProfile == null) {
             return "";
         }
         return selectedProfile.getRealname();
     }
 
     /**
      * Sets the realname in the active profile. This method will do nothing if there is no active
      * profile.
      *
      * @param realname New profile real name
      */
     public void setRealname(final String realname) {
         if (selectedProfile != null) {
             selectedProfile.setRealname(realname);
             pcs.firePropertyChange("realname", null, null);
         }
     }
 
     /**
      * Retrieves the ident of the active profile. This method will return an empty string if there
      * is no active profile.
      *
      * @return Active profile ident or an empty string if there is no active profile
      */
     public String getIdent() {
         if (selectedProfile == null) {
             return "";
         }
         return selectedProfile.getIdent();
     }
 
     /**
      * Sets the ident of the active profile. This method will do nothing if there is no active
      * profile.
      *
      * @param ident New profile ident
      */
     public void setIdent(final String ident) {
         if (selectedProfile != null) {
             selectedProfile.setIdent(ident);
             pcs.firePropertyChange("ident", null, null);
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
      * @return true if all other validators pass and there is at least one profile
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
        return new ProfileRenameValidator(displayedProfiles, selectedProfile).validate(
                 selectedProfile.getName());
     }
 
     /**
      * Are the nicknames in the active profile valid? If there is no active profile the validation
      * passes.
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
      * Is the realname in the active profile valid? If there is no active profile the validation
      * passes.
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
      * Is the ident in the active profile valid? If there is no active profile the validation
      * passes.
      *
      * @return passes the ident is valid
      */
     public ValidationResponse isIdentValid() {
         if (selectedProfile == null) {
             return new ValidationResponse();
         }
         if (selectedProfile.getIdent() == null
                 || selectedProfile.getIdent().isEmpty()) {
             return new ValidationResponse();
         }
         return new IdentValidator().validate(selectedProfile.getIdent());
     }
 
     /**
      * This method saves any changes made in the model to disk. All profiles marked for deletion are
      * removed and then all remaining profiles are have their save method called.
      */
     public void save() {
         for (Profile profile : profiles) {
             if (profile.isDeleted()) {
                 profile.delete();
             } else {
                 profile.save();
             }
         }
     }
 
     /**
      * Adds a property change listener to all property change events in the model.
      *
      * @param listener Listener to add
      */
     public void addPropertyChangeListener(final PropertyChangeListener listener) {
         pcs.addPropertyChangeListener(listener);
     }
 
     /**
      * Adds a property change listener to a given property on this model.
      *
      * @param propertyName Property to listen on
      * @param listener     Listener to add
      */
     public void addPropertyChangeListener(final String propertyName,
             final PropertyChangeListener listener) {
         pcs.addPropertyChangeListener(propertyName, listener);
     }
 
 }
