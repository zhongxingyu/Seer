 /*******************************************************************************
 * Copyright (c) 2011 Nokia Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Comarch team - initial API and implementation
 *******************************************************************************/
 package org.ned.client.view.renderer;
 
 import com.sun.lwuit.Component;
 import com.sun.lwuit.Container;
 import com.sun.lwuit.Font;
 import com.sun.lwuit.Label;
 import com.sun.lwuit.List;
 import com.sun.lwuit.layouts.BoxLayout;
 import com.sun.lwuit.list.ListCellRenderer;
 import com.sun.lwuit.plaf.Border;
 import org.ned.client.NedMidlet;
 import org.ned.client.NedResources;
 import org.ned.client.transfer.DownloadTask;
 import org.ned.client.view.customComponents.ProgressBar;
 import org.ned.client.view.style.NEDStyleToolbox;
 
 public class DownloadTaskCellRenderer extends Container implements ListCellRenderer {
 
     private static Border mSelectedBorder = Border.createBevelLowered( NEDStyleToolbox.BACKGROUND_END_COLOR,
                                                                        NEDStyleToolbox.BACKGROUND_END_COLOR,
                                                                        NEDStyleToolbox.BACKGROUND_END_COLOR,
                                                                        NEDStyleToolbox.BACKGROUND_END_COLOR );
 
     private static Border mUnselectedBorder =  Border.createBevelLowered( NEDStyleToolbox.DARK_GREY,
                                                                           NEDStyleToolbox.DARK_GREY,
                                                                           NEDStyleToolbox.DARK_GREY,
                                                                           NEDStyleToolbox.DARK_GREY );
 
     private Label mTitleLabel;
     private Label mTransferTitle;
     private Label mStatusLabel;
     private Label mTransferStatus;
     private Label mFileSizeLabel;
     private Label mFileSizeStatus;
     private ProgressBar mProgressbar;
 
     public DownloadTaskCellRenderer() {
         setLayout(new BoxLayout(BoxLayout.Y_AXIS));
         setCellRenderer(true);
         Font resultFont = NedMidlet.getFont(NedResources.LIST_FONT);
 
         Container fileContainer = new Container(new BoxLayout(BoxLayout.X_AXIS));
         fileContainer.getStyle().setPadding(0, 0, 0, 0);
         fileContainer.setFocusable(true);
         fileContainer.setCellRenderer(true);
 
         mTitleLabel = new Label(NedResources.TRA_TITLE);
         mTitleLabel.getStyle().setPadding(0, 0, 0, 0);
         mTitleLabel.getStyle().setFgColor( NEDStyleToolbox.MAIN_FONT_COLOR );
         mTitleLabel.setFocusable(false);
         mTitleLabel.setCellRenderer(true);
 
         mTransferTitle = new Label("");
         mTransferTitle.getStyle().setFont(resultFont);
         mTransferTitle.getStyle().setFgColor( NEDStyleToolbox.MAIN_FONT_COLOR );
         mTransferTitle.getStyle().setPadding(0, 0, 0, 0);
         mTransferTitle.setFocusable(false);
         mTransferTitle.setCellRenderer(true);
 
         fileContainer.addComponent(mTitleLabel);
         fileContainer.addComponent(mTransferTitle);
 
         Container statusContainer = new Container(new BoxLayout(BoxLayout.X_AXIS));
         statusContainer.getStyle().setPadding(0, 0, 0, 0);
         statusContainer.setFocusable(false);
         statusContainer.setCellRenderer(true);
 
         Container sizeContainer = new Container(new BoxLayout(BoxLayout.X_AXIS));
         sizeContainer.getStyle().setPadding(0, 0, 0, 0);
         sizeContainer.setFocusable(true);
         sizeContainer.setCellRenderer(true);
 
         mFileSizeLabel = new Label( NedResources.TRA_FILESIZE );
         mFileSizeLabel.getStyle().setFgColor(NEDStyleToolbox.MAIN_FONT_COLOR);
         mFileSizeLabel.getStyle().setPadding(0, 0, 0, 0);
         mFileSizeLabel.setFocusable(false);
         mFileSizeLabel.setCellRenderer(true);
 
         mFileSizeStatus = new Label(NedResources.TRA_UNKNOWN_SIZE);
         mFileSizeStatus.getStyle().setFont(resultFont);
         mFileSizeStatus.getStyle().setFgColor( NEDStyleToolbox.MAIN_FONT_COLOR );
         mFileSizeStatus.getStyle().setPadding(0, 0, 0, 0);
         mFileSizeStatus.setFocusable(false);
         mFileSizeStatus.setCellRenderer(true);
 
         sizeContainer.addComponent(mFileSizeLabel);
         sizeContainer.addComponent(mFileSizeStatus);
 
         mStatusLabel = new Label(NedResources.TRA_STATUS);
         mStatusLabel.getStyle().setFgColor(NEDStyleToolbox.MAIN_FONT_COLOR);
         mStatusLabel.getStyle().setPadding(0, 0, 0, 0);
         mStatusLabel.setFocusable(false);
         mStatusLabel.setCellRenderer(true);
 
         mTransferStatus = new Label(NedResources.TRA_WAITING_STATUS);
         mTransferStatus.getStyle().setFont(resultFont);
         mTransferStatus.getStyle().setFgColor( NEDStyleToolbox.MAIN_FONT_COLOR );
         mTransferStatus.getStyle().setPadding(0, 0, 0, 0);
         mTransferStatus.setFocusable(false);
         mTransferStatus.setCellRenderer(true);
 
         statusContainer.addComponent(mStatusLabel);
         statusContainer.addComponent(mTransferStatus);
 
         mProgressbar = new ProgressBar();
         mProgressbar.setFocusable(false);
         mProgressbar.setCellRenderer(true);
 
         addComponent(fileContainer);
         addComponent(sizeContainer);
         addComponent(statusContainer);
         addComponent(mProgressbar);
     }
 
     public String getUIID() {
         return "TransferContainer";
     }
 
     public Component getListCellRendererComponent( List list, Object value, int i, boolean isSelected ) {
         if( value == null ) {
            return null;
         }
         DownloadTask content = (DownloadTask)value;
         mTransferTitle.setText( content.getTitle() );
         mTransferStatus.setText( content.getStatus() );
         mFileSizeStatus.setText( content.getDownloadLength() != 0 ? content.getDownloadLength()/1024 + " kB" : NedResources.TRA_UNKNOWN_SIZE );
         mProgressbar.setProgress( content.getPercentDownloadedInt() );
 
         if ( isSelected) {
             setFocus( true );
             mTransferTitle.getStyle().setFgColor( NEDStyleToolbox.MAIN_FONT_COLOR );
             mTransferStatus.getStyle().setFgColor( NEDStyleToolbox.MAIN_FONT_COLOR );
             getStyle().setBorder( mSelectedBorder );
         } else {
             setFocus( false );
             mTransferTitle.getStyle().setFgColor( NEDStyleToolbox.MAIN_FONT_COLOR );
             mTransferStatus.getStyle().setFgColor( NEDStyleToolbox.MAIN_FONT_COLOR );
             getStyle().setBorder( mUnselectedBorder );
         }
         return this;
     }
 
     public Component getListFocusComponent(List list) {
         return null;
     }
 }
