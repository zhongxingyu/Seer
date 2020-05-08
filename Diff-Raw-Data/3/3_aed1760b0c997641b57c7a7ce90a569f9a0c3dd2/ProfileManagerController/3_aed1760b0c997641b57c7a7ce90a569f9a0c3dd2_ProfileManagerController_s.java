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
 
 import com.dmdirc.interfaces.config.IdentityFactory;
 
 /**
  * This controller reacts to all actions in the profile manager dialog.
  */
 public class ProfileManagerController {
 
     /** Dialog shown to user. */
     private final ProfileManagerDialog dialog;
     /** Model used to store state. */
     private final ProfileManagerModel model;
     /** Factory to use when creating new profiles. */
     private final IdentityFactory identityFactory;
 
     public ProfileManagerController(final ProfileManagerDialog dialog,
             final ProfileManagerModel model, final IdentityFactory identityFactory) {
         this.dialog = dialog;
         this.model = model;
         this.identityFactory = identityFactory;
     }
 
     /**
      * Adds a new profile.
      *
      * @param name Name of the profile
      */
     public void addProfile(final String name) {
         model.addProfile(new Profile(name, identityFactory));
     }
 
     /** Deletes the active profile. */
     public void deleteProfile() {
         model.deleteProfile((Profile) model.getSelectedProfile());
     }
 
     /**
      * Adds the specified nickname.
      *
      * @param nickname New nickname
      */
     public void addNickname(final String nickname) {
         model.addNickname(nickname);
     }
 
     /**
      * Edits the selected nickname.
      *
      * @param nickname New nickname
      */
     public void editNickname(final String nickname) {
         model.editNickname((String) model.getSelectedNickname(), nickname);
     }
 
     /**
      * Deletes the selected nickname.
      */
     public void deleteNickname() {
         model.deleteNickname(model.getSelectedNickname());
     }
 
     /**
      * Closes the dialog.
      */
     public void closeDialog() {
         dialog.dispose();
     }
 
     /**
      * Saves the model and closes the dialog.
      */
     public void saveAndCloseDialog() {
         model.save();
         closeDialog();
     }
 
 }
