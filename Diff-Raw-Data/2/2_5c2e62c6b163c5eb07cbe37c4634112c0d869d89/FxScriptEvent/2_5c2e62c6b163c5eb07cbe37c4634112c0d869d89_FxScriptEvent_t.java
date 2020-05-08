 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared.scripting;
 
 import com.flexive.shared.exceptions.FxNotFoundException;
 
 /**
  * All kinds of scripts used in flexive,
  * depending on the type scripts are initialized with different bindings
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public enum FxScriptEvent {
 
     /**
      * Manually execute script, not dependent on any 'trigger point' - will be executed on demand.
      * <p>Execution of a manual script requires the the calling user to be in the role
      * {@link com.flexive.shared.security.Role#ScriptExecution}.</p>
      */
     Manual(1, FxScriptScope.All),
 
     /**
      * Fired before an existing content is saved<br/><br/>
      * Passed variables:<br/>
      * <i>FxContent content</i>: content instance which is about to be saved<br/>
      */
     BeforeContentSave(2, FxScriptScope.Type,
             "FxContent content"),
 
     /**
      * Fired after an existing content is saved<br/><br/>
      * Passed variables:<br/>
      * <i>FxPK pk</i>: Primary key of the saved content<br/>
      */
     AfterContentSave(3, FxScriptScope.Type,
             "FxPK pk"),
 
     /**
      * Fired before a new content is created<br/><br/>
      * Passed variables:<br/>
      * <i>FxContent content</i>: content instance which is about to be saved<br/>
      */
     BeforeContentCreate(4, FxScriptScope.Type,
             "FxContent content"),
 
     /**
      * Fired after a new content is created<br/><br/>
      * Passed variables:<br/>
      * <i>FxPK pk</i>: Primary key of the created content<br/>
      */
     AfterContentCreate(5, FxScriptScope.Type,
             "FxPK pk"),
 
     /**
      * Fired before a content is removed<br/><br/>
      * Passed variables:<br/>
      * <i>FxPK pk</i>: Primary key of the removed content<br/>
      */
     BeforeContentRemove(6, FxScriptScope.Type,
             "FxPK pk", "FxContentSecurityInfo securityInfo"),
 
     /**
      * Fired after a content is removed<br/><br/>
      * Passed variables:<br/>
      * <i>FxPK pk</i>: Primary key of the removed content<br/>
      */
     AfterContentRemove(7, FxScriptScope.Type,
             "FxPK pk"),
 
     /**
      * Fired after a content is loaded
      * <br/><br/>
      * Passed variables:<br/>
      * <i>FxContent content</i>: the loaded content instance<br/>
      */
     AfterContentLoad(8, FxScriptScope.Type,
             "FxContent content"),
 
     /**
      * Fired after a new content instance is initialized with default values<br/><br/>
      * Passed variables:<br/>
      * <i>FxContent content</i>: the initialized content instance<br/>
      */
     AfterContentInitialize(9, FxScriptScope.Type,
             "FxContent content"),
 
     /**
      * Fired for ContentEngine.prepareSave() for new contents<br/><br/>
      * Passed variables:<br/>
      * <i>FxContent content</i>: the content to be prepared for creation<br/>
      */
     PrepareContentCreate(10, FxScriptScope.Type,
             "FxContent content"),
 
     /**
      * Fired for ContentEngine.prepareSave() for existing contents<br/><br/>
      * Passed variables:<br/>
      * <i>FxContent content</i>: the content to be prepared for saving<br/>
      */
     PrepareContentSave(11, FxScriptScope.Type,
             "FxContent content"),
 
     /**
      * Fired before a FxData update of an existing instance<br/><br/>
      * Passed variables:<br/>
      * <i>FxContent content</i>: the content to be updated<br/>
      * <i>FxDeltaChange change</i>: information about the change<br/>
      */
     BeforeDataChangeUpdate(12, FxScriptScope.Assignment,
             "FxContent content", "FxDeltaChange change"),
 
     /**
      * Fired after a FxData update of an existing instance<br/><br/>
      * Passed variables:<br/>
      * <i>FxContent content</i>: the content to be updated<br/>
      * <i>FxDeltaChange change</i>: information about the change<br/>
      */
     AfterDataChangeUpdate(13, FxScriptScope.Assignment,
             "FxContent content", "FxDeltaChange change"),
 
     /**
      * Fired before a FxData is removed from an existing instance, instance will <b>not</b> be removed!<br/><br/>
      * Passed variables:<br/>
      * <i>FxContent content</i>: the content to be updated<br/>
      * <i>FxDeltaChange change</i>: information about the change<br/>
      */
     BeforeDataChangeDelete(14, FxScriptScope.Assignment,
             "FxContent content", "FxDeltaChange change"),
 
     /**
      * Fired after a FxData is removed from an existing instance, instance will <b>not</b> be removed!<br/><br/>
      * Passed variables:<br/>
      * <i>FxContent content</i>: the content to be updated<br/>
      * <i>FxDeltaChange change</i>: information about the change<br/>
      */
     AfterDataChangeDelete(15, FxScriptScope.Assignment,
             "FxContent content", "FxDeltaChange change"),
 
     /**
      * Fired before a FxData is updated in an existing instance<br/><br/>
      * Passed variables:<br/>
      * <i>FxContent content</i>: the content to be updated<br/>
      * <i>FxDeltaChange change</i>: information about the change<br/>
      */
     BeforeDataChangeAdd(16, FxScriptScope.Assignment,
             "FxContent content", "FxDeltaChange change"),
 
     /**
      * Fired after a FxData is updated in an existing instance<br/><br/>
      * Passed variables:<br/>
      * <i>FxContent content</i>: the content to be updated<br/>
      * <i>FxDeltaChange change</i>: information about the change<br/>
      */
     AfterDataChangeAdd(17, FxScriptScope.Assignment,
             "FxContent content", "FxDeltaChange change"),
 
     /**
      * Fired before a new instance is created that will contain the assignment<br/><br/>
      * Passed variables:<br/>
      * <i>FxContent content</i>: the content to be created<br/>
      * <i>FxAssignment assignment</i>: the new assignment<br/>
      */
     BeforeAssignmentDataCreate(18, FxScriptScope.Assignment,
             "FxContent content", "FxAssignment assignment"),
 
     /**
      * Fired after a new instance is created that will contain the assignment<br/><br/>
      * Passed variables:<br/>
      * <i>FxPK pk</i>: primary key of the created content<br/>
      * <i>FxAssignment assignment</i>: the new assignment<br/>
      */
     AfterAssignmentDataCreate(19, FxScriptScope.Assignment,
             "FxPK pk", "FxAssignment assignment"),
 
     /**
      * Fired before an existing instance is saved that contains the assignment<br/><br/>
      * Passed variables:<br/>
      * <i>FxContent content</i>: the content to be saved<br/>
      * <i>FxAssignment assignment</i>: the updated assignment<br/>
      */
     BeforeAssignmentDataSave(20, FxScriptScope.Assignment,
             "FxContent content", "FxAssignment assignment"),
 
     /**
      * Fired after an existing instance is saved that contains the assignment<br/><br/>
      * Passed variables:<br/>
      * <i>FxPK pk</i>: primary key of the saved content<br/>
      * <i>FxAssignment assignment</i>: the updated assignment<br/>
      */
     AfterAssignmentDataSave(21, FxScriptScope.Assignment,
             "FxPK pk", "FxAssignment assignment"),
 
 
     /**
      * Fired before an instance is deleted <b>could</b> contain the assignment,
      * no guarantee can be made that the instance actually contains FxData for the assignment!<br/><br/>
      * Passed variables:<br/>
      * <i>FxPK pk</i>: primary key of the removed content<br/>
      * <i>FxAssignment assignment</i>: the assignment<br/>
      */
     BeforeAssignmentDataDelete(22, FxScriptScope.Assignment,
             "FxPK pk", "FxAssignment assignment"),
 
     /**
      * Fired after an instance is deleted <b>could</b> contain the assignment,
      * no guarantee can be made that the instance actually contains FxData for the assignment!<br/><br/>
      * Passed variables:<br/>
      * <i>FxPK pk</i>: primary key of the created content<br/>
      * <i>FxAssignment assignment</i>: the assignment<br/>
      */
     AfterAssignmentDataDelete(23, FxScriptScope.Assignment,
             "FxPK pk", "FxAssignment assignment"),
 
     /**
      * Fired for processing of binaries.
      * <br/><br/>
      * Passed variables:<br/>
      * <i>boolean processedk</i>: has the binary already been processed? Usually return from the script if true<br/>
      * <i>boolean useDefaultPreview</i>: set from the script if a default preview should be used or the script creates one<br/>
      * <i>int defaultId</i>: set from the script: the default id to use for previews if using thed default preview id<br/>
      * <i>String mimeType</i>: set from the script: mime type<br/>
      * <i>String metaData</i>: set from the script: xml based meta data<br/>
      * <i>File binaryFile</i>: the original binary file<br/>
      * <i>File previewFile1</i>: set from script: file containing preview 1<br/>
      * <i>File previewFile2</i>: set from script: file containing preview 2<br/>
      * <i>File previewFile3</i>: set from script: file containing preview 3<br/>
      * <i>int[] dimensionPreview1</i>: set from script: dimension of preview 1<br/>
      * <i>int[] dimensionPreview2</i>: set from script: dimension of preview 2<br/>
      * <i>int[] dimensionPreview3</i>: set from script: dimension of preview 3<br/>
      */
     BinaryPreviewProcess(24, FxScriptScope.BinaryProcessing,
             "boolean processed",
             "boolean useDefaultPreview",
             "int defaultId",
             "String mimeType",
             "String metaData",
             "File binaryFile",
             "File previewFile1",
             "File previewFile2",
             "File previewFile3",
             "int[] dimensionPreview1",
             "int[] dimensionPreview2",
             "int[] dimensionPreview3"
     ),
 
     /**
      * Fired after a new user account has been created. The account ID and the
      * contact data PK are bound as "accountId" and "pk" .<br/><br/>
      * Passed variables:<br/>
      * <i>long accountId</i>: id of the account<br/>
      * <i>FxPK pk</i>: primary key of the contact data<br/>
      */
     AfterAccountCreate(25, FxScriptScope.Accounts, "long accountId", "FxPK pk"),
 
     /**
      * Fired after a new node was added to the tree.
      * Note that this event will not be fired from the <code>populate(mode)</code> method!<br/><br/>
      * Passed variables:<br/>
      * <i>FxTreeNode node</i>: the affected tree node<br/>
      */
     AfterTreeNodeAdded(26, FxScriptScope.Tree, "FxTreeNode node"),
 
     /**
      * Fired after a new node was removed from the tree.
      * Note that this event will not be fired from the <code>clear(mode)</code> method!<br/><br/>
      * Passed variables:<br/>
      * <i>FxTreeNode node</i>: the affected tree node<br/>
      */
     AfterTreeNodeRemoved(27, FxScriptScope.Tree, "FxTreeNode node"),
 
     /**
      * Fired after a new node was removed from the tree.
      * Note that this event will not be fired from the <code>clear(mode)</code> method!<br/><br/>
      * Passed variables:<br/>
      * <i>FxTreeNode node</i>: the affected tree node<br/>
      */
     BeforeTreeNodeRemoved(28, FxScriptScope.Tree, "FxTreeNode node"),
 
     /**
      * Fired after a node was activated (copied from Edit to Live tree).
      * The node passed as binding is the activated node from the Live tree!
      * Please note that there is no deactivate event since this is equal to removal.<br/><br/>
      * Passed variables:<br/>
      * <i>FxTreeNode node</i>: the affected tree node<br/>
      */
     AfterTreeNodeActivated(29, FxScriptScope.Tree, "FxTreeNode node"),
 
     /**
      * Fired before a node is activated (copied from Edit to Live tree).
      * The node passed as binding is the node to be activated from the Edit tree!
      * Please note that there is no deactivate event since this is equal to removal.<br/><br/>
      * Passed variables:<br/>
      * <i>FxTreeNode node</i>: the affected tree node<br/>
      */
     BeforeTreeNodeActivated(30, FxScriptScope.Tree, "FxTreeNode node"),
 
     /**
      * Fired after a content reference has been replaced by a folder reference.
      * This event happens when a content that is referenced by the tree is removed and is no leaf node.<br/><br/>
      * Passed variables:<br/>
      * <i>FxTreeNode node</i>: the affected tree node<br/>
      * <i>FxPK content</i>: primary key of the removed content<br/>
      */
     AfterTreeNodeFolderReplacement(31, FxScriptScope.Tree, "FxTreeNode node", "FxPK content"),
 
     /**
      * Fired when an account logs in. The script has to return a UserTicket for the logged in user.
      * If no scripts of this event type exist, the default database based login is performed which is skipped on
      * presence of such scripts!
      * Use this event if you want to authenticate against LDAP or the like.
      * The callback provides access to the datasource, ejb context and if a session should be "taken over" to
      * prevent multiple logins.<br/><br/>
      * Passed variables:<br/>
      * <i>String loginname</i>: Name used to log on<br/>
      * <i>String password</i>: Plaintext password<br/>
      * <i>FxCallback callback</i>: An FxCallback instance providing a DataSource to access the database, the EJB context and a flag if user already logged in with this account should be logged off<br/>
      */
     AccountLogin(32, FxScriptScope.Accounts, "String loginname", "String password", "FxCallback callback"),
 
     /**
      * Fired when an account is logged off to clear state information if needed.
      * If no scripts of this event type exist, the default database based logout is performed which is skipped on
      * presence of such scripts!
      * Passed variables:<br/>
      * <i>UserTicket ticket</i>: The UserTicket of the user that is being logged off<br/>
      */
     AccountLogout(33, FxScriptScope.Accounts, "UserTicket ticket"),
 
     /**
     * An internal event for scripts executable by any user.
      */
     Internal(34, FxScriptScope.Type);
 
 
     private long id;
     private FxScriptScope scope;
     private String[] bindingInfo;
 
     /**
      * Constructore
      *
      * @param id          type id
      * @param scope       script scope
      * @param bindingInfo provided variable bindings
      */
     FxScriptEvent(int id, FxScriptScope scope, String... bindingInfo) {
         this.id = id;
         this.scope = scope;
         this.bindingInfo = bindingInfo;
     }
 
     /**
      * Get the scope of this script, ie if it is independent, for types or assignments
      *
      * @return scope
      */
     public FxScriptScope getScope() {
         return scope;
     }
 
     /**
      * Get a list of all bindings available to the script.
      * Binding descriptions are human readable in the form "datatype variablename"
      *
      * @return list of all bindings available to the script
      */
     public String[] getBindingInfo() {
         return bindingInfo.clone();
     }
 
     /**
      * Getter for the internal id
      *
      * @return internal id
      */
     public long getId() {
         return id;
     }
 
     public String getName() {
         if (id >= 1) {
             try {
                 return getById(id).name();
             } catch (FxNotFoundException e) {
                 return "Not_Defined";
             }
         }
         return "";
     }
 
     /**
      * Get a FxScriptEvent by its id
      *
      * @param id requested id
      * @return FxScriptEvent
      * @throws FxNotFoundException on errors
      */
     public static FxScriptEvent getById(long id) throws FxNotFoundException {
         for (FxScriptEvent event : FxScriptEvent.values())
             if (event.id == id)
                 return event;
         throw new FxNotFoundException("ex.scripting.type.notFound", id);
     }
 }
