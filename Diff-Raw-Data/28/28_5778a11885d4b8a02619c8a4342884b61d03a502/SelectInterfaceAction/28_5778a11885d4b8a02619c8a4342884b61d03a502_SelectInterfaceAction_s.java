 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.epita.mti.plic.opensource.controlibserversample.controller;
 
 import com.epita.mti.plic.opensource.controlibserversample.view.QRCodeView;
 import com.google.zxing.WriterException;
import java.awt.Choice;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 /**
  *
  * @author Julien "Roulyo" Fraisse
  */
 public class SelectInterfaceAction implements ItemListener
 {
   private QRCodeView view;
  
   public SelectInterfaceAction(QRCodeView view)
   {
     this.view = view;
   }
  
   @Override
   public void itemStateChanged(ItemEvent e)
   {
     try
     {
       view.setQrcode((String) e.getItem());
     }
     catch (WriterException ex)
     {
       ex.printStackTrace();
     }
   }
  
 }
