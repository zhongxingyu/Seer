 /**********************************************************************
  * $Source: /cvsroot/hibiscus/hibiscus.xmlrpc/src/de/willuhn/jameica/hbci/xmlrpc/server/LastschriftServiceImpl.java,v $
 * $Revision: 1.4 $
 * $Date: 2007/09/10 16:09:32 $
  * $Author: willuhn $
  * $Locker:  $
  * $State: Exp $
  *
  * Copyright (c) by willuhn.webdesign
  * All rights reserved
  *
  **********************************************************************/
 
 package de.willuhn.jameica.hbci.xmlrpc.server;
 
 import java.rmi.RemoteException;
 
 import de.willuhn.jameica.hbci.rmi.Lastschrift;
 import de.willuhn.jameica.hbci.xmlrpc.rmi.LastschriftService;
 import de.willuhn.logging.Logger;
 import de.willuhn.util.ApplicationException;
 
 /**
  * Implementierung des Lastschrift-Service.
  */
 public class LastschriftServiceImpl extends AbstractTransferServiceImpl implements
     LastschriftService
 {
 
   /**
    * ct.
    * @throws RemoteException
    */
   public LastschriftServiceImpl() throws RemoteException
   {
     super();
   }
 
 
   /**
    * @see de.willuhn.datasource.Service#getName()
    */
   public String getName() throws RemoteException
   {
     return "[xml-rpc] lastschrift";
   }
 
 
   /**
    * @see de.willuhn.jameica.hbci.xmlrpc.server.AbstractTransferServiceImpl#getTransferType()
    */
   Class getTransferType()
   {
     return Lastschrift.class;
   }
 
   /**
    * @see de.willuhn.jameica.hbci.xmlrpc.rmi.LastschriftService#create(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, double, java.lang.String, java.lang.String)
    */
   public String create(String kontoID, String kto, String blz, String name, String zweck, String zweck2, double betrag, String termin, String type) throws RemoteException
   {
     try
     {
       Lastschrift l = (Lastschrift) createObject(kontoID,kto,blz,name,zweck,zweck2,betrag,termin);
      l.setTyp(type);
       l.store();
       return null;
     }
     catch (ApplicationException ae)
     {
       return ae.getMessage();
     }
     catch (RemoteException re)
     {
       Logger.error("unable to create transfer",re);
       return i18n.tr("Fehler beim Erstellen der Lastschrift: {0}",re.getMessage());
     }
   }
 
 
   /**
    * @see de.willuhn.jameica.hbci.xmlrpc.rmi.TransferService#create(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, double, java.lang.String)
    */
   public String create(String kontoID, String kto, String blz, String name, String zweck, String zweck2, double betrag, String termin) throws RemoteException
   {
     return create(kontoID,kto,blz,name,zweck,zweck2,betrag,termin,"05");
   }
 }
 
 
 /*********************************************************************
  * $Log: LastschriftServiceImpl.java,v $
  * Revision 1.4  2007/09/10 16:09:32  willuhn
  * @N Termin in XML-RPC Connector fuer Auftraege
  *
  * Revision 1.3  2007/07/24 14:49:57  willuhn
  * @N Neuer Paramater "zweck2"
  *
  * Revision 1.2  2007/06/04 12:49:05  willuhn
  * @N Angabe des Typs bei Lastschriften
  *
  * Revision 1.1  2006/11/16 22:11:26  willuhn
  * @N Added lastschrift support
  *
  **********************************************************************/
