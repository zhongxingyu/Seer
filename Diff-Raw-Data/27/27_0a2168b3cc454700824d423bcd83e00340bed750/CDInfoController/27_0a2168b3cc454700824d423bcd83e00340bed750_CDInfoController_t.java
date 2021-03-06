 package ch.cyberduck.ui.cocoa;
 
 /*
  *  Copyright (c) 2005 David Kocher. All rights reserved.
  *  http://cyberduck.ch/
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  Bug fixes, suggestions and comments should be sent to:
  *  dkocher@cyberduck.ch
  */
 
 import com.apple.cocoa.application.*;
 import com.apple.cocoa.foundation.*;
 
 import ch.cyberduck.core.*;
 
 import org.apache.log4j.Logger;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @version $Id$
  */
 public class CDInfoController extends CDWindowController {
     private static Logger log = Logger.getLogger(CDInfoController.class);
 
     private List files;
 
     // ----------------------------------------------------------
     // Outlets
     // ----------------------------------------------------------
 
     private NSTextField filenameField; //IBOutlet
 
     public void setFilenameField(NSTextField filenameField) {
         this.filenameField = filenameField;
         NSNotificationCenter.defaultCenter().addObserver(this,
                 new NSSelector("filenameInputDidEndEditing", new Class[]{NSNotification.class}),
                 NSControl.ControlTextDidEndEditingNotification,
                 filenameField);
     }
 
     private NSTextField groupField; //IBOutlet
 
     public void setGroupField(NSTextField groupField) {
         this.groupField = groupField;
     }
 
     private NSTextField kindField; //IBOutlet
 
     public void setKindField(NSTextField kindField) {
         this.kindField = kindField;
     }
 
 //    private NSTextField mimeField; //IBOutlet
 //
 //    public void setMimeField(NSTextField mimeField) {
 //        this.mimeField = mimeField;
 //    }
 
     private NSTextField modifiedField; //IBOutlet
 
     public void setModifiedField(NSTextField modifiedField) {
         this.modifiedField = modifiedField;
     }
 
     private NSTextField ownerField; //IBOutlet
 
     public void setOwnerField(NSTextField ownerField) {
         this.ownerField = ownerField;
     }
 
     private NSTextField sizeField; //IBOutlet
 
     public void setSizeField(NSTextField sizeField) {
         this.sizeField = sizeField;
     }
 
     private NSTextField pathField; //IBOutlet
 
     public void setPathField(NSTextField pathField) {
         this.pathField = pathField;
     }
 
     private NSBox permissionsBox; //IBOutlet
 
     public void setPermissionsBox(NSBox permissionsBox) {
         this.permissionsBox = permissionsBox;
     }
 
     private NSButton recursiveCheckbox;
 
     public void setRecursiveCheckbox(NSButton recursiveCheckbox) {
         this.recursiveCheckbox = recursiveCheckbox;
         this.recursiveCheckbox.setState(NSCell.OffState);
     }
 
     private NSButton applyButton;
 
     public void setApplyButton(NSButton applyButton) {
         this.applyButton = applyButton;
         this.applyButton.setTarget(this);
         this.applyButton.setAction(new NSSelector("applyButtonClicked", new Class[]{Object.class}));
     }
 
     private NSButton sizeButton;
 
     public void setSizeButton(NSButton sizeButton) {
         this.sizeButton = sizeButton;
         this.sizeButton.setTarget(this);
         this.sizeButton.setAction(new NSSelector("sizeButtonClicked", new Class[]{Object.class}));
     }
 
     private NSProgressIndicator sizeProgress; // IBOutlet
 
     public void setSizeProgress(final NSProgressIndicator sizeProgress) {
         this.sizeProgress = sizeProgress;
         this.sizeProgress.setDisplayedWhenStopped(false);
         this.sizeProgress.setStyle(NSProgressIndicator.ProgressIndicatorSpinningStyle);
     }
 
     private NSProgressIndicator permissionProgress; // IBOutlet
 
     public void setPermissionProgress(final NSProgressIndicator permissionProgress) {
         this.permissionProgress = permissionProgress;
         this.permissionProgress.setDisplayedWhenStopped(false);
         this.permissionProgress.setStyle(NSProgressIndicator.ProgressIndicatorSpinningStyle);
     }
 
     public NSButton ownerr; //IBOutlet
     public NSButton ownerw; //IBOutlet
     public NSButton ownerx; //IBOutlet
     public NSButton groupr; //IBOutlet
     public NSButton groupw; //IBOutlet
     public NSButton groupx; //IBOutlet
     public NSButton otherr; //IBOutlet
     public NSButton otherw; //IBOutlet
     public NSButton otherx; //IBOutlet
 
     private NSImageView iconImageView; //IBOutlet
 
     public void setIconImageView(NSImageView iconImageView) {
         this.iconImageView = iconImageView;
     }
 
     public void setWindow(NSWindow window) {
         super.setWindow(window);
         this.window.setReleasedWhenClosed(false);
     }
 
     public void windowWillClose(NSNotification notification) {
         this.window().endEditingForObject(null);
         if(Preferences.instance().getBoolean("browser.info.isInspector")) {
             //Do not mark this controller as invalid if it should be used again 
             return;
         }
         super.windowWillClose(notification);
     }
 
     // ----------------------------------------------------------
     // Constructors
     // ----------------------------------------------------------
 
     public static class Factory {
         private static Map open = new HashMap();
 
         public static CDInfoController create(final CDBrowserController controller, final List files) {
             if(open.containsKey(files)) {
                 return (CDInfoController) open.get(files);
             }
             final CDInfoController c = new CDInfoController(controller, files) {
                 public void windowWillClose(NSNotification notification) {
                     Factory.open.remove(files);
                     super.windowWillClose(notification);
                 }
             };
             open.put(files, c);
             return c;
         }
     }
 
     private CDBrowserController controller;
 
     private CDInfoController(final CDBrowserController controller, List files) {
         this.controller = controller;
         this.controller.addListener(new CDWindowListener() {
             public void windowWillClose() {
                 final NSWindow window = window();
                 if(null != window) {
                     window.close();
                 }
             }
         });
         this.loadBundle();
         this.setFiles(files);
     }
 
     protected String getBundleName() {
         return "Info";
     }
 
     public void setFiles(List files) {
         this.files = files;
         this.init();
     }
 
     private static NSPoint cascadedWindowPoint;
 
     public void awakeFromNib() {
         if(null == cascadedWindowPoint) {
             cascadedWindowPoint = this.window.cascadeTopLeftFromPoint(this.window.frame().origin());
         }
         else {
             cascadedWindowPoint = this.window.cascadeTopLeftFromPoint(cascadedWindowPoint);
         }
         this.ownerr.setTarget(this);
         this.ownerr.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
         this.ownerr.setAllowsMixedState(true);
         this.ownerw.setTarget(this);
         this.ownerw.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
         this.ownerw.setAllowsMixedState(true);
         this.ownerx.setTarget(this);
         this.ownerx.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
         this.ownerx.setAllowsMixedState(true);
 
         this.groupr.setTarget(this);
         this.groupr.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
         this.groupr.setAllowsMixedState(true);
         this.groupw.setTarget(this);
         this.groupw.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
         this.groupw.setAllowsMixedState(true);
         this.groupx.setTarget(this);
         this.groupx.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
         this.groupx.setAllowsMixedState(true);
 
         this.otherr.setTarget(this);
         this.otherr.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
         this.otherr.setAllowsMixedState(true);
         this.otherw.setTarget(this);
         this.otherw.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
         this.otherw.setAllowsMixedState(true);
         this.otherx.setTarget(this);
         this.otherx.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
         this.otherx.setAllowsMixedState(true);
     }
 
     private void init() {
         this.applyButton.setEnabled(controller.isConnected());
 
        final int count = this.numberOfFiles();
        if(count > 0) {
             Path file = (Path) this.files.get(0);
            this.filenameField.setStringValue(count > 1 ? "(" + NSBundle.localizedString("Multiple files", "") + ")" :
                     file.getName());
            this.filenameField.setEnabled(1 == count);
             if(file.attributes.isSymbolicLink() && file.getSymbolicLinkPath() != null) {
                 this.pathField.setAttributedStringValue(new NSAttributedString(file.getSymbolicLinkPath(),
                         TRUNCATE_MIDDLE_ATTRIBUTES));
             }
             else {
                 this.pathField.setAttributedStringValue(new NSAttributedString(file.getParent().getAbsolute(),
                         TRUNCATE_MIDDLE_ATTRIBUTES));
             }
            this.groupField.setStringValue(count > 1 ? "(" + NSBundle.localizedString("Multiple files", "") + ")" :
                     file.attributes.getGroup());
            if(count > 1) {
                 this.kindField.setStringValue("(" + NSBundle.localizedString("Multiple files", "") + ")");
 //                this.mimeField.setStringValue("(" + NSBundle.localizedString("Multiple files", "") + ")");
             }
             else {
                 this.kindField.setAttributedStringValue(new NSAttributedString(file.kind(),
                         TRUNCATE_MIDDLE_ATTRIBUTES));
 //                this.mimeField.setAttributedStringValue(new NSAttributedString(file.getMimeType(),
 //                        TRUNCATE_MIDDLE_ATTRIBUTES));
             }
            if(count > 1) {
                 this.modifiedField.setStringValue("(" + NSBundle.localizedString("Multiple files", "") + ")");
             }
             else {
                 if(-1 == file.attributes.getModificationDate()) {
                     this.modifiedField.setAttributedStringValue(new NSAttributedString(
                             NSBundle.localizedString("Unknown", ""),
                             TRUNCATE_MIDDLE_ATTRIBUTES));
 
                 }
                 else {
                     this.modifiedField.setAttributedStringValue(new NSAttributedString(
                             CDDateFormatter.getLongFormat(file.attributes.getModificationDate(), file.getHost().getTimezone()),
                             TRUNCATE_MIDDLE_ATTRIBUTES));
                 }
             }
            this.ownerField.setStringValue(count > 1 ? "(" + NSBundle.localizedString("Multiple files", "") + ")" :
                     file.attributes.getOwner());
             this.sizeButton.setEnabled(file.attributes.isDirectory());
             this.updateSize();
             this.initPermissionsCheckbox(false);
             Permission permission = null;
             for(Iterator i = files.iterator(); i.hasNext();) {
                 permission = ((AbstractPath) i.next()).attributes.getPermission();
                 log.debug("Permission:" + permission);
                 if(null == permission) {
                     this.initPermissionsCheckbox(false);
                     applyButton.setEnabled(false);
                     recursiveCheckbox.setEnabled(false);
                     break;
                 }
                 else {
                     this.updatePermisssionsCheckbox(ownerr, permission.getOwnerPermissions()[Permission.READ]);
                     this.updatePermisssionsCheckbox(ownerw, permission.getOwnerPermissions()[Permission.WRITE]);
                     this.updatePermisssionsCheckbox(ownerx, permission.getOwnerPermissions()[Permission.EXECUTE]);
 
                     this.updatePermisssionsCheckbox(groupr, permission.getGroupPermissions()[Permission.READ]);
                     this.updatePermisssionsCheckbox(groupw, permission.getGroupPermissions()[Permission.WRITE]);
                     this.updatePermisssionsCheckbox(groupx, permission.getGroupPermissions()[Permission.EXECUTE]);
 
                     this.updatePermisssionsCheckbox(otherr, permission.getOtherPermissions()[Permission.READ]);
                     this.updatePermisssionsCheckbox(otherw, permission.getOtherPermissions()[Permission.WRITE]);
                     this.updatePermisssionsCheckbox(otherx, permission.getOtherPermissions()[Permission.EXECUTE]);
                 }
             }
 
             //		octalField.setStringValue(""+file.getOctalCode());
            if(count > 1) {
                 this.permissionsBox.setTitle(NSBundle.localizedString("Permissions", "")
                         + " | " + "(" + NSBundle.localizedString("Multiple files", "") + ")");
             }
             else {
                 this.permissionsBox.setTitle(NSBundle.localizedString("Permissions", "")
                         + " | " + (null == permission ? NSBundle.localizedString("Unknown", "") :permission.toString()));
             }
 
             NSImage fileIcon = null;
            if(count > 1) {
                 fileIcon = NSImage.imageNamed("multipleDocuments32.tiff");
             }
             else {
                 fileIcon = CDIconCache.instance().iconForPath(file, 32);
             }
             this.iconImageView.setImage(fileIcon);
         }
     }
 
     private void initPermissionsCheckbox(boolean enabled) {
         ownerr.setEnabled(enabled);
         ownerr.setState(NSCell.OffState);
         ownerw.setEnabled(enabled);
         ownerw.setState(NSCell.OffState);
         ownerx.setEnabled(enabled);
         ownerx.setState(NSCell.OffState);
         groupr.setEnabled(enabled);
         groupr.setState(NSCell.OffState);
         groupw.setEnabled(enabled);
         groupw.setState(NSCell.OffState);
         groupx.setEnabled(enabled);
         groupx.setState(NSCell.OffState);
         otherr.setEnabled(enabled);
         otherr.setState(NSCell.OffState);
         otherw.setEnabled(enabled);
         otherw.setState(NSCell.OffState);
         otherx.setEnabled(enabled);
         otherx.setState(NSCell.OffState);
     }
 
     private void updatePermisssionsCheckbox(NSButton checkbox, boolean condition) {
         // Sets the cell's state to value, which can be NSCell.OnState, NSCell.OffState, or NSCell.MixedState.
         // If necessary, this method also redraws the receiver.
         if((checkbox.state() == NSCell.OffState || !checkbox.isEnabled()) && !condition) {
             checkbox.setState(NSCell.OffState);
         }
         else if((checkbox.state() == NSCell.OnState || !checkbox.isEnabled()) && condition) {
             checkbox.setState(NSCell.OnState);
         }
         else {
             checkbox.setState(NSCell.MixedState);
         }
         checkbox.setEnabled(true);
     }
 
     private int numberOfFiles() {
         return null == files ? 0 : files.size();
     }
 
     public void filenameInputDidEndEditing(NSNotification sender) {
         if(this.numberOfFiles() == 1) {
             final Path current = (Path) this.files.get(0);
             if(!this.filenameField.stringValue().equals(current.getName())) {
                 if(this.filenameField.stringValue().indexOf('/') == -1) {
                     final Path renamed = PathFactory.createPath(controller.workdir().getSession(),
                             current.getParent().getAbsolute(), this.filenameField.stringValue(), current.attributes.getType());
                     controller.renamePath(current, renamed);
                 }
                 else if(!StringUtils.hasText(filenameField.stringValue())) {
                     this.filenameField.setStringValue(current.getName());
                 }
                 else {
                     this.alert(NSAlertPanel.informationalAlertPanel(
                             NSBundle.localizedString("Error", "Alert sheet title"),
                             NSBundle.localizedString("Invalid character in filename.", ""), // message
                             NSBundle.localizedString("OK", "Alert default button"), // defaultbutton
                             null, //alternative button
                             null //other button
                     ));
                 }
             }
         }
     }
 
     private Permission getPermissionFromSelection() {
         boolean[][] p = new boolean[3][3];
 
         p[Permission.OWNER][Permission.READ] = (ownerr.state() == NSCell.OnState);
         p[Permission.OWNER][Permission.WRITE] = (ownerw.state() == NSCell.OnState);
         p[Permission.OWNER][Permission.EXECUTE] = (ownerx.state() == NSCell.OnState);
 
         p[Permission.GROUP][Permission.READ] = (groupr.state() == NSCell.OnState);
         p[Permission.GROUP][Permission.WRITE] = (groupw.state() == NSCell.OnState);
         p[Permission.GROUP][Permission.EXECUTE] = (groupx.state() == NSCell.OnState);
 
         p[Permission.OTHER][Permission.READ] = (otherr.state() == NSCell.OnState);
         p[Permission.OTHER][Permission.WRITE] = (otherw.state() == NSCell.OnState);
         p[Permission.OTHER][Permission.EXECUTE] = (otherx.state() == NSCell.OnState);
 
         return new Permission(p);
     }
 
     public void permissionSelectionChanged(final NSButton sender) {
         if(sender.state() == NSCell.MixedState) {
             sender.setState(NSCell.OnState);
         }
         final Permission permission = this.getPermissionFromSelection();
         permissionsBox.setTitle(NSBundle.localizedString("Permissions", "") + " | " + permission.toString());
     }
 
     public void applyButtonClicked(final Object sender) {
         log.debug("applyButtonClicked");
         this.applyButton.setEnabled(false);
         this.permissionProgress.startAnimation(null);
         final Permission permission = this.getPermissionFromSelection();
         // send the changes to the remote host
         controller.background(new BrowserBackgroundAction(controller) {
             public void run() {
                 for(Iterator i = files.iterator(); i.hasNext();) {
                     final AbstractPath next = (AbstractPath) i.next();
                     next.writePermissions(permission,
                             recursiveCheckbox.state() == NSCell.OnState);
                     if(!controller.isConnected()) {
                         break;
                     }
                     next.getParent().invalidate();
                 }
             }
 
             public void cleanup() {
                 controller.reloadData(true);
                 applyButton.setEnabled(true);
                 permissionProgress.stopAnimation(null);
             }
         });
     }
 
     public void sizeButtonClicked(final Object sender) {
         log.debug("sizeButtonClicked");
         this.sizeButton.setEnabled(false);
         this.sizeProgress.startAnimation(null);
         // send the changes to the remote host
         controller.background(new BrowserBackgroundAction(controller) {
             public void run() {
                 for(Iterator i = files.iterator(); i.hasNext();) {
                     this.calculateSize((Path) i.next());
                     if(!controller.isConnected()) {
                         break;
                     }
                 }
             }
 
             public void cleanup() {
                 controller.reloadData(true);
                 updateSize();
                 sizeButton.setEnabled(true);
                 sizeProgress.stopAnimation(null);
             }
 
             /**
              * Calculates recursively the size of this path
              *
              * @return The size of the file or the sum of all containing files if a directory
              * @warn Potentially lengthy operation
              */
             private double calculateSize(Path p) {
                 if(p.attributes.isDirectory()) {
                     long size = 0;
                     for(Iterator iter = p.childs().iterator(); iter.hasNext();) {
                         size += this.calculateSize((Path) iter.next());
                     }
                     p.attributes.setSize(size);
                 }
                 return p.attributes.getSize();
             }
         });
     }
 
     /**
      * Updates the size field by iterating over all files and
      * rading the cached size value in the attributes of the path
      */
     private void updateSize() {
         long size = 0;
         for(Iterator i = files.iterator(); i.hasNext();) {
             size += ((AbstractPath) i.next()).attributes.getSize();
         }
         this.sizeField.setAttributedStringValue(
                 new NSAttributedString(Status.getSizeAsString(size) + " (" + size + " bytes)",
                         TRUNCATE_MIDDLE_ATTRIBUTES));
     }
 }
