 /*
  * IT Consol Professional Services S.A.C.
  * Todo los derechos reservados (C) COPYRIGHT
  *
  * Revision History
  *
  * Change Date  Changed By          Request#      Comment
  * ----------------------------------------------------------------------------
  * 2013-09-26   Enrique Kishimoto   -             Create class
  *
  */
 package itc.plusmc.app.inventory;
 
 import java.rmi.RemoteException;
 import java.util.Date;
 import psdi.app.currency.CurrencyService;
 import psdi.app.inventory.InvBalances;
 import psdi.app.inventory.InvCost;
 import psdi.app.inventory.Inventory;
 import psdi.app.inventory.InventorySetRemote;
 import psdi.app.inventory.MatRecTrans;
 import psdi.app.item.ItemRemote;
 import psdi.app.location.LocationRemote;
 import psdi.app.location.LocationSetRemote;
 import psdi.mbo.MboRemote;
 import psdi.mbo.MboSet;
 import psdi.mbo.MboSetRemote;
 import psdi.mbo.SqlFormat;
 import psdi.server.AppService;
 import psdi.util.MXApplicationException;
 import psdi.util.MXException;
 import psdi.util.logging.MXLogger;
 import psdi.util.logging.MXLoggerFactory;
 
 /**
  *
  * @author ekishimoto
  */
 public class ITCPlusMCMatRecTrans extends MatRecTrans implements ITCPlusMCMatRecTransRemote {
 
   private final MXLogger log = MXLoggerFactory.getLogger("maximo.customization");
 
   /**
    * Construct the ITCPlusMCMatRecTrans object
    *
    * @param ms a MboSet
    * @throws MXException
    * @throws RemoteException
    */
   public ITCPlusMCMatRecTrans(MboSet ms) throws MXException, RemoteException {
     super(ms);
   }
 
   /**
    * Saves the MatRecTrans object and all other linked objects <BR>
    * Might update inventory balance/cost, MR, or work order Calls super.save()
    * after work on the other objects
    *
    * @throws psdi.util.MXException
    * @throws java.rmi.RemoteException
    * <BR>
    * <table border=2>
    * <TR>
    * <TH>GROUP</TH>
    * <TH>KEY</TH>
    * <TH>REASON
    * <TH>
    * <TR>
    * <TD>inventory</TD>
    * <TD>mxcollabRC </TD>
    * <TD>if the poMbo is not null and the invoice number is null and the the
    * adding of receipt is not allowed</TD>
    * </TABLE>
    */
   @Override
   public void save() throws MXException, RemoteException {
     log.debug("<<<< Entering itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:save() >>>>");
     String fromsiteidMC = getFromSiteIdMC();
     String tositeidMC = getToSiteIdMC();
 
     log.debug(">>>> Call this.setFromOldValues() method");
     setFromOldValues();
     log.debug(">>>> Call this.setToOldValues() method");
     setToOldValues();
     log.debug(">>>> Call this.setFromOldValuesMC(fromsiteidMC) method");
     setFromOldValuesMC(fromsiteidMC);
     log.debug(">>>> Call this.setToOldValuesMC(tositeidMC) method");
     setToOldValuesMC(tositeidMC);
 
     log.debug(">>>> Call super.save() method");
     super.save();
     log.debug(">>>> Call this.setCurrentValuesMC(tositeidMC) method");
     setCurrentValuesMC(tositeidMC);
     log.debug(">>>> Call this.updateAverageCostMC(tositeidMC) method");
     updateAverageCostMC(tositeidMC);
 
     log.debug(">>>> Call this.setFromNewValues() method");
     setFromNewValues();
     log.debug(">>>> Call this.setToNewValues() method");
     setToNewValues();
     log.debug(">>>> Call this.setFromNewValuesMC(fromsiteidMC) method");
     setFromNewValuesMC(fromsiteidMC);
     log.debug(">>>> Call this.setToNewValuesMC(tositeidMC) method");
     setToNewValuesMC(tositeidMC);
     log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:save() >>>>");
   }
 
   /**
    * Este metodo almacena, en los atributos ITCFROMOLDBAL y ITCFROMOLDAVGCOST
    * respectivamente, el balance y el costo promedio del item del almacen origen
    * antes de que se registre dicha transaccion y se actualice el inventario.
    *
    * @throws MXException
    * @throws RemoteException
    */
   private void setFromOldValues() throws MXException, RemoteException {
     log.debug("<<<< Entering itc.plusmc.app.inventory.multicurrency.ITCPlusMCMatRecTrans:setFromOldValues() >>>>");
     if (isFromStore() && ((isNew() && isNull("STATUS")) || (isModified() && getString("STATUS").equalsIgnoreCase("COMP")))) {
       log.debug(">>>> isFromStore() && ((isNew() && isNull(\"STATUS\")) || (isModified() && getString(\"STATUS\").equalsIgnoreCase(\"COMP\")))");
       String frombin = getString("FROMBIN");
       String fromlot = getString("FROMLOT");
       String fromconditioncode = getString("FROMCONDITIONCODE");
       String fromstoreloc = getString("FROMSTORELOC");
       String fromsiteid = getString("FROMSITEID");
 
       log.debug(">>>> MATRECTRANS.FROMBIN: " + frombin);
       log.debug(">>>> MATRECTRANS.FROMLOT: " + fromlot);
       log.debug(">>>> MATRECTRANS.FROMCONDITIONCODE: " + fromconditioncode);
       log.debug(">>>> MATRECTRANS.FROMSTORELOC: " + fromstoreloc);
       log.debug(">>>> MATRECTRANS.FROMSITEID: " + fromsiteid);
 
       Inventory inventory = (Inventory) getSharedInventory(fromstoreloc, fromsiteid);
 
       if (inventory != null) {
         log.debug(">>>> inventory != null");
         InvBalances invbalances = inventory.getInvBalanceRecord(frombin, fromlot, fromconditioncode, fromstoreloc, fromsiteid);
         MboRemote invcost = inventory.getInvCostRecord(fromconditioncode);
 
         if (invbalances != null) {
           log.debug(">>>> invbalances != null");
           setValue("ITCFROMOLDBAL", invbalances.getCurrentBalance(), NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
           log.debug(">>>> MATRECTRANS.ITCFROMOLDBAL: " + Double.toString(getDouble("ITCFROMOLDBAL")));
         }
         if (invcost != null) {
           log.debug(">>>> invcost != null");
           setValue("ITCFROMOLDAVGCOST", invcost.getDouble("AVGCOST"), NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
           log.debug(">>>> MATRECTRANS.ITCFROMOLDAVGCOST: " + Double.toString(getDouble("ITCFROMOLDAVGCOST")));
         }
       }
     }
     log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:setFromOldValues() >>>>");
   }
 
   /**
    * Este metodo almacena, en los atributos ITCTOOLDBAL y ITCTOOLDAVGCOST
    * respectivamente, el balance y el costo promedio del item del almacen
    * destino antes de que se registre dicha transaccion y se actualice el
    * inventario.
    *
    * @throws MXException
    * @throws RemoteException
    */
   private void setToOldValues() throws MXException, RemoteException {
     log.debug("<<<< Entering itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:setToOldValues() >>>>");
     if (isStore() && ((isNew() && isNull("STATUS")) || (isModified() && getString("STATUS").equalsIgnoreCase("COMP")))) {
       log.debug(">>>> isStore() && ((isNew() && isNull(\"STATUS\")) || (isModified() && getString(\"STATUS\").equalsIgnoreCase(\"COMP\")))");
       String tobin = getString("TOBIN");
       String tolot = getString("TOLOT");
       String conditioncode = getString("CONDITIONCODE");
       String tostoreloc = getString("TOSTORELOC");
       String siteid = getString("SITEID");
 
       log.debug(">>>> MATRECTRANS.TOBIN: " + tobin);
       log.debug(">>>> MATRECTRANS.TOLOT: " + tolot);
       log.debug(">>>> MATRECTRANS.CONDITIONCODE: " + conditioncode);
       log.debug(">>>> MATRECTRANS.TOSTORELOC: " + tostoreloc);
       log.debug(">>>> MATRECTRANS.SITEID: " + siteid);
 
       Inventory inventory = (Inventory) getSharedInventory(tostoreloc, siteid);
 
       if (inventory != null) {
         log.debug(">>>> inventory != null");
         InvBalances invbalances = inventory.getInvBalanceRecord(tobin, tolot, conditioncode, tostoreloc, siteid);
         MboRemote invcost = inventory.getInvCostRecord(conditioncode);
 
         if (invbalances != null) {
           log.debug(">>>> invbalances != null");
           setValue("ITCTOOLDBAL", invbalances.getCurrentBalance(), NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
           log.debug(">>>> MATRECTRANS.ITCTOOLDBAL: " + Double.toString(getDouble("ITCTOOLDBAL")));
         }
         if (invcost != null) {
           log.debug(">>>> invcost != null");
           setValue("ITCTOOLDAVGCOST", invcost.getDouble("AVGCOST"), NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
           log.debug(">>>> MATRECTRANS.ITCTOOLDAVGCOST: " + Double.toString(getDouble("ITCTOOLDAVGCOST")));
         }
       }
     }
     log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:setToOldValues() >>>>");
   }
 
   /**
    * Este metodo almacena, en los atributos ITCFROMNEWBAL y ITCFROMNEWAVGCOST
    * respectivamente, el balance y el costo promedio del item del almacen origen
    * despues de que se registre dicha transaccion y se actualice el inventario.
    *
    * @throws MXException
    * @throws RemoteException
    */
   private void setFromNewValues() throws MXException, RemoteException {
     log.debug("<<<< Entering itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:setFromNewValues() >>>>");
     if (isFromStore() && ((isNew() && isNull("STATUS")) || (isModified() && getString("STATUS").equalsIgnoreCase("COMP")))) {
       log.debug(">>>> isFromStore() && ((isNew() && isNull(\"STATUS\")) || (isModified() && getString(\"STATUS\").equalsIgnoreCase(\"COMP\")))");
       String frombin = getString("FROMBIN");
       String fromlot = getString("FROMLOT");
       String fromconditioncode = getString("FROMCONDITIONCODE");
       String fromstoreloc = getString("FROMSTORELOC");
       String fromsiteid = getString("FROMSITEID");
 
       log.debug(">>>> MATRECTRANS.FROMBIN: " + frombin);
       log.debug(">>>> MATRECTRANS.FROMLOT: " + fromlot);
       log.debug(">>>> MATRECTRANS.FROMCONDITIONCODE: " + fromconditioncode);
       log.debug(">>>> MATRECTRANS.FROMSTORELOC: " + fromstoreloc);
       log.debug(">>>> MATRECTRANS.FROMSITEID: " + fromsiteid);
 
       Inventory inventory = (Inventory) getSharedInventory(fromstoreloc, fromsiteid);
 
       if (inventory != null) {
         log.debug(">>> inventory != null");
         InvBalances invbalances = inventory.getInvBalanceRecord(frombin, fromlot, fromconditioncode, fromstoreloc, fromsiteid);
         MboRemote invcost = inventory.getInvCostRecord(fromconditioncode);
 
         if (invbalances != null) {
           log.debug(">>>> invbalances != null");
           setValue("ITCFROMNEWBAL", invbalances.getCurrentBalance(), NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
           log.debug(">>>> MATRECTRANS.ITCFROMNEWBAL: " + Double.toString(getDouble("ITCFROMNEWBAL")));
         }
         if (invcost != null) {
           log.debug(">>>> invcost != null");
           setValue("ITCFROMNEWAVGCOST", invcost.getDouble("AVGCOST"), NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
           log.debug(">>>> MATRECTRANS.ITCFROMNEWAVGCOST: " + Double.toString(getDouble("ITCFROMNEWAVGCOST")));
         }
       }
     }
     log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:setFromNewValues() >>>>");
   }
 
   /**
    * Este metodo almacena, en los atributos ITCTONEWBAL y ITCTONEWAVGCOST
    * respectivamente, el balance y el costo promedio del item del almacen
    * destino despues de que se registre dicha transaccion y se actualice el
    * inventario.
    *
    * @throws MXException
    * @throws RemoteException
    */
   private void setToNewValues() throws MXException, RemoteException {
     log.debug("<<<< Entering itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:setToNewValues() >>>>");
     if (isStore() && ((isNew() && isNull("STATUS")) || (isModified() && getString("STATUS").equalsIgnoreCase("COMP")))) {
       log.debug(">>>> isStore() && ((isNew() && isNull(\"STATUS\")) || (isModified() && getString(\"STATUS\").equalsIgnoreCase(\"COMP\")))");
       String tobin = getString("TOBIN");
       String tolot = getString("TOLOT");
       String conditioncode = getString("CONDITIONCODE");
       String tostoreloc = getString("TOSTORELOC");
       String siteid = getString("SITEID");
 
       log.debug(">>>> MATRECTRANS.TOBIN: " + tobin);
       log.debug(">>>> MATRECTRANS.TOLOT: " + tolot);
       log.debug(">>>> MATRECTRANS.CONDITIONCODE: " + conditioncode);
       log.debug(">>>> MATRECTRANS.TOSTORELOC: " + tostoreloc);
       log.debug(">>>> MATRECTRANS.SITEID: " + siteid);
 
       Inventory inventory = (Inventory) getSharedInventory(tostoreloc, siteid);
 
       if (inventory != null) {
         log.debug(">>>> inventory != null");
         InvBalances invbalances = inventory.getInvBalanceRecord(tobin, tolot, conditioncode, tostoreloc, siteid);
         MboRemote invcost = inventory.getInvCostRecord(conditioncode);
 
         if (invbalances != null) {
           log.debug(">>>> invbalances != null");
           setValue("ITCTONEWBAL", invbalances.getCurrentBalance(), NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
           log.debug(">>>> MATRECTRANS.ITCTONEWBAL: " + Double.toString(getDouble("ITCTONEWBAL")));
         }
         if (invcost != null) {
           log.debug(">>>> invcost != null");
           setValue("ITCTONEWAVGCOST", invcost.getDouble("AVGCOST"), NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
           log.debug(">>>> MATRECTRANS.ITCTONEWAVGCOST: " + Double.toString(getDouble("ITCTONEWAVGCOST")));
         }
       }
     }
     log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:setToNewValues() >>>>");
   }
 
   /**
    * Este metodo almacena el costo unitario y el costo de linea de la
    * transaccion para la multimoneda
    *
    * @param siteidMC MultiCurrency Site Id
    * @throws MXException
    * @throws RemoteException
    */
   private void setCurrentValuesMC(String siteidMC) throws MXException, RemoteException {
     log.debug("<<<< Entering itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:setCurrentValuesMC(siteidMC) >>>>");
     if (siteidMC != null && ((isNew() && isNull("STATUS")) || (isModified() && getString("STATUS").equalsIgnoreCase("COMP")))) {
       log.debug(">>>> siteidMC != null && ((isNew() && isNull(\"STATUS\")) || (isModified() && getString(\"STATUS\").equalsIgnoreCase(\"COMP\")))");
       CurrencyService curService = (CurrencyService) ((AppService) getMboServer()).getMXServer().lookup("CURRENCY");
       double quantity = getDouble("QUANTITY");
       Date exchangeDate = getDate("TRANSDATE");
       String orgId = getString("ORGID");
       String currencyCodeFrom = getString("CURRENCYCODE");
       String currencyCodeTo = curService.getBaseCurrency2(orgId, getUserInfo());
       String fromconditioncode = getString("FROMCONDITIONCODE");
       String fromstoreloc = getString("FROMSTORELOC");
       String fromsiteid = getString("FROMSITEID");
       double itcmcUnitCost = 0.00D;
       double itcmcActualCost = 0.00D;
       double itcmcLoadedCost = 0.00D;
       double itcmcLineCost = 0.00D;
 
       log.debug(">>>> siteidMC: " + siteidMC);
       log.debug(">>>> MATRECTRANS.QUANTITY: " + Double.toString(quantity));
       log.debug(">>>> MATRECTRANS.TRANSDATE: " + exchangeDate.toString());
       log.debug(">>>> MATRECTRANS.ORGID: " + orgId);
       log.debug(">>>> MATRECTRANS.CURRENCYCODE: " + currencyCodeFrom);
       log.debug(">>>> ORGANIZATION.BASECURRENCY2: " + currencyCodeTo);
       log.debug(">>>> MATRECTRANS.FROMCONDITIONCODE: " + fromconditioncode);
       log.debug(">>>> MATRECTRANS.FROMSTORELOC: " + fromstoreloc);
       log.debug(">>>> MATRECTRANS.FROMSITEID: " + fromsiteid);
 
       if (isReceipt()) {
         log.debug(">>>> isReceipt()");
         double unitcost = getDouble("UNITCOST");
         double actualcost = getDouble("ACTUALCOST");
         double linecost = getDouble("LINECOST");
         double loadedcost = getDouble("LOADEDCOST");
 
         log.debug(">>>> MATRECTRANS.UNITCOST: " + Double.toString(unitcost));
         log.debug(">>>> MATRECTRANS.ACTUALCOST: " + Double.toString(actualcost));
         log.debug(">>>> MATRECTRANS.LINECOST: " + Double.toString(linecost));
         log.debug(">>>> MATRECTRANS.LOADEDCOST: " + Double.toString(loadedcost));
 
         itcmcUnitCost = curService.calculateCurrencyCost(getUserInfo(), currencyCodeFrom, currencyCodeTo, unitcost, exchangeDate, orgId);
         itcmcActualCost = curService.calculateCurrencyCost(getUserInfo(), currencyCodeFrom, currencyCodeTo, actualcost, exchangeDate, orgId);
         itcmcLineCost = curService.calculateCurrencyCost(getUserInfo(), currencyCodeFrom, currencyCodeTo, linecost, exchangeDate, orgId);
         itcmcLoadedCost = curService.calculateCurrencyCost(getUserInfo(), currencyCodeFrom, currencyCodeTo, loadedcost, exchangeDate, orgId);
       } else if (isReturn()) {
         log.debug(">>>> isReturn()");
         MboSetRemote originalMRTSet = getMboSet("ORIGINALRECEIPT");
 
         if (originalMRTSet != null && !originalMRTSet.isEmpty()) {
           log.debug(">>>> originalMRTSet != null && !originalMRTSet.isEmpty()");
           MboRemote originalMRT = originalMRTSet.getMbo(0);
 
           if (originalMRT != null) {
             log.debug(">>>> originalMRT != null");
             itcmcUnitCost = originalMRT.getDouble("ITCMCUNITCOST");
             itcmcActualCost = originalMRT.getDouble("ITCMCACTUALCOST");
             itcmcLineCost = quantity * itcmcUnitCost;
             itcmcLoadedCost = quantity * itcmcUnitCost;
           }
         }
       } else if (isVoidReceipt()) {
         log.debug(">>>> isVoidReceipt()");
         MboSetRemote originalMRTSet = getMboSet("ORIGINALRECEIPT");
 
         if (originalMRTSet != null && !originalMRTSet.isEmpty()) {
           log.debug(">>>> originalMRTSet != null && !originalMRTSet.isEmpty()");
           MboRemote originalMRT = originalMRTSet.getMbo(0);
 
           if (originalMRT != null) {
             log.debug(">>>> originalMRT != null");
             itcmcUnitCost = originalMRT.getDouble("ITCMCUNITCOST");
             itcmcActualCost = originalMRT.getDouble("ITCMCACTUALCOST");
             itcmcLineCost = quantity * itcmcUnitCost;
             itcmcLoadedCost = quantity * itcmcUnitCost;
           }
         }
       } else if (isTransfer()) {
         log.debug(">>>> isTransfer()");
 
         if (!isNull("PONUM") && isNull("INVUSEID")) {
           log.debug(">>>> !isNull(\"PONUM\") && isNull(\"INVUSEID\")");
           double unitcost = getDouble("UNITCOST");
           double actualcost = getDouble("ACTUALCOST");
           double linecost = getDouble("LINECOST");
           double loadedcost = getDouble("LOADEDCOST");
 
           log.debug(">>>> MATRECTRANS.UNITCOST: " + Double.toString(unitcost));
           log.debug(">>>> MATRECTRANS.ACTUALCOST: " + Double.toString(actualcost));
           log.debug(">>>> MATRECTRANS.LINECOST: " + Double.toString(linecost));
           log.debug(">>>> MATRECTRANS.LOADEDCOST: " + Double.toString(loadedcost));
 
           itcmcUnitCost = curService.calculateCurrencyCost(getUserInfo(), currencyCodeFrom, currencyCodeTo, unitcost, exchangeDate, orgId);
           itcmcActualCost = curService.calculateCurrencyCost(getUserInfo(), currencyCodeFrom, currencyCodeTo, actualcost, exchangeDate, orgId);
 
           if (isNull("ROTASSETNUM")) {
             log.debug(">>>> isNull(\"ROTASSETNUM\")");
             itcmcLineCost = curService.calculateCurrencyCost(getUserInfo(), currencyCodeFrom, currencyCodeTo, linecost, exchangeDate, orgId);
             itcmcLoadedCost = curService.calculateCurrencyCost(getUserInfo(), currencyCodeFrom, currencyCodeTo, loadedcost, exchangeDate, orgId);
           } else {
             log.debug(">>>> !isNull(\"ROTASSETNUM\")");
             itcmcLineCost = 0.00D;
             itcmcLoadedCost = 0.00D;
           }
         } else {
           log.debug(">>>> !(!isNull(\"PONUM\") && isNull(\"INVUSEID\"))");
           Inventory inventoryMC = (Inventory) getSharedInventory(fromstoreloc, siteidMC);
 
           if (inventoryMC != null) {
             log.debug(">>>> inventoryMC != null");
             MboRemote invcostMC = inventoryMC.getInvCostRecord(fromconditioncode);
 
             if (invcostMC != null) {
               log.debug(">>>> invcostMC != null");
               itcmcUnitCost = invcostMC.getDouble("AVGCOST");
               itcmcActualCost = itcmcUnitCost;
               itcmcLineCost = quantity * itcmcUnitCost;
               itcmcLoadedCost = itcmcLineCost;
             }
           }
         }
       } else if (isShipTransfer()) {
         log.debug(">>>> isShipTransfer()");
         Inventory inventoryMC = (Inventory) getSharedInventory(fromstoreloc, siteidMC);
 
         if (inventoryMC != null) {
           log.debug(">>>> inventoryMC != null");
           MboRemote invcostMC = inventoryMC.getInvCostRecord(fromconditioncode);
 
           if (invcostMC != null) {
             log.debug(">>>> invcostMC != null");
             itcmcUnitCost = invcostMC.getDouble("AVGCOST");
             itcmcActualCost = itcmcUnitCost;
             itcmcLineCost = quantity * itcmcUnitCost;
             itcmcLoadedCost = itcmcLineCost;
           }
         }
       } else if (isShipCancel()) {
         log.debug(">>>> isShipCancel()");
         MboSetRemote originalMRTSet = getMboSet("ORIGINALSHIPTRANSFER");
 
         if (originalMRTSet != null && !originalMRTSet.isEmpty()) {
           log.debug(">>>> originalMRTSet != null && !originalMRTSet.isEmpty()");
           MboRemote originalMRT = originalMRTSet.getMbo(0);
 
           if (originalMRT != null) {
             log.debug(">>>> originalMRT != null");
             itcmcUnitCost = originalMRT.getDouble("ITCMCUNITCOST");
             itcmcActualCost = itcmcUnitCost;
             itcmcLineCost = quantity * itcmcUnitCost;
             itcmcLoadedCost = itcmcLineCost;
           }
         }
       } else if (isShipReceipt()) {
         log.debug(">>>> isShipReceipt()");
         MboSetRemote originalMRTSet = getMboSet("ORIGINALSHIPTRANSFER");
 
         if (originalMRTSet != null && !originalMRTSet.isEmpty()) {
           log.debug(">>>> originalMRTSet != null && !originalMRTSet.isEmpty()");
           MboRemote originalMRT = originalMRTSet.getMbo(0);
 
           if (originalMRT != null) {
             log.debug(">>>> originalMRT != null");
             itcmcUnitCost = originalMRT.getDouble("ITCMCUNITCOST");
             itcmcActualCost = itcmcUnitCost;
             itcmcLineCost = quantity * itcmcUnitCost;
             itcmcLoadedCost = itcmcLineCost;
           }
         }
       } else if (isShipReturn()) {
         log.debug(">>>> isShipReturn()");
         MboSetRemote originalMRTSet = getMboSet("ITCORIGINALSHIPRECEIPT");
 
         if (originalMRTSet != null && !originalMRTSet.isEmpty()) {
           log.debug(">>>> originalMRTSet != null && !originalMRTSet.isEmpty()");
           MboRemote originalMRT = originalMRTSet.getMbo(0);
 
           if (originalMRT != null) {
             log.debug(">>>> originalMRT != null");
             itcmcUnitCost = originalMRT.getDouble("ITCMCUNITCOST");
             itcmcActualCost = itcmcUnitCost;
             itcmcLineCost = quantity * itcmcUnitCost;
             itcmcLoadedCost = itcmcLineCost;
           }
         }
       } else if (isVoidShipReceipt()) {
         log.debug(">>>> isVoidShipReceipt()");
         MboSetRemote originalMRTSet = getMboSet("ITCORIGINALSHIPRECEIPT");
 
         if (originalMRTSet != null && !originalMRTSet.isEmpty()) {
           log.debug(">>>> originalMRTSet != null && !originalMRTSet.isEmpty()");
           MboRemote originalMRT = originalMRTSet.getMbo(0);
 
           if (originalMRT != null) {
             log.debug(">>>> originalMRT != null");
             itcmcUnitCost = originalMRT.getDouble("ITCMCUNITCOST");
             itcmcActualCost = itcmcUnitCost;
             itcmcLineCost = quantity * itcmcUnitCost;
             itcmcLoadedCost = itcmcLineCost;
           }
         }
       } else if (isStageTransfer()) {
         log.debug(">>>> isStageTransfer()");
 
         if (!isNull("RECEIPTREF")) {
           log.debug(">>>> !isNull(\"RECEIPTREF\")");
           Inventory inventoryMC = (Inventory) getSharedInventory(fromstoreloc, siteidMC);
 
           if (inventoryMC != null) {
             log.debug(">>>> inventoryMC != null");
             MboRemote invcostMC = inventoryMC.getInvCostRecord(fromconditioncode);
 
             if (invcostMC != null) {
               log.debug(">>>> invcostMC != null");
               itcmcUnitCost = invcostMC.getDouble("AVGCOST");
               itcmcActualCost = itcmcUnitCost;
               itcmcLineCost = quantity * itcmcUnitCost;
               itcmcLoadedCost = itcmcLineCost;
             }
           }
         } else {
           log.debug(">>>> isNull(\"RECEIPTREF\")");
           MboSetRemote originalMRTSet = getMboSet("ITCORIGINALSTAGETRANSFER");
 
           if (originalMRTSet != null && !originalMRTSet.isEmpty()) {
             log.debug(">>>> originalMRTSet != null && !originalMRTSet.isEmpty()");
             MboRemote originalMRT = originalMRTSet.getMbo(0);
 
             if (originalMRT != null) {
               log.debug(">>>> originalMRT != null");
               itcmcUnitCost = originalMRT.getDouble("ITCMCUNITCOST");
               itcmcActualCost = itcmcUnitCost;
               itcmcLineCost = quantity * itcmcUnitCost;
               itcmcLoadedCost = itcmcLineCost;
             }
           }
         }
       } else if (isKitMake()) {
         log.debug(">>>> isKitMake()");
         Inventory toInventory = (Inventory) getSharedInventory(fromstoreloc, fromsiteid);
 
         if (toInventory != null) {
           log.debug(">>>> toInventory != null");
           MboSetRemote itemstructSet = toInventory.getMboSet("FIRSTLEVELKITSTRUCT");
 
           if (itemstructSet != null && !itemstructSet.isEmpty()) {
             log.debug(">>>> itemstructSet != null && !itemstructSet.isEmpty()");
             double value = 0.00D;
 
             for (MboRemote itemstruct = itemstructSet.moveFirst(); itemstruct != null; itemstruct = itemstructSet.moveNext()) {
               log.debug(">>>> for itemstruct iteration");
 
               if (!itemstruct.getString("ITEMNUM").equals(getString("ITEMNUM"))) {
                 log.debug(">>>> !itemstruct.getString(\"ITEMNUM\").equals(getString(\"ITEMNUM\"))");
                 MboSetRemote itemstructInventorySetMC = itemstruct.getMboSet("$INVENTORY", "INVENTORY", "itemnum=:itemnum and itemsetid=:itemsetid and location='" + fromstoreloc + "' and siteid='" + siteidMC + "'");
 
                 if (itemstructInventorySetMC != null && !itemstructInventorySetMC.isEmpty()) {
                   log.debug(">>>> itemstructInventorySetMC != null && !itemstructInventorySetMC.isEmpty()");
                   Inventory itemstructInventoryMC = (Inventory) itemstructInventorySetMC.getMbo(0);
 
                   if (itemstructInventoryMC != null) {
                     log.debug(">>>> itemstructInventoryMC != null");
                     InvCost itemstructInvCostMC = (InvCost) getInvCostRecordMC(itemstructInventoryMC, siteidMC);
 
                     if (itemstructInvCostMC != null) {
                       log.debug(">>>> itemstructInvCostMC != null");
                       value += (itemstructInvCostMC.getDouble("AVGCOST") * itemstruct.getDouble("QUANTITY"));
                     }
                   }
                 }
               }
             }
             log.debug(">>>> value: " + Double.toString(value));
             itcmcUnitCost = value;
             itcmcActualCost = itcmcUnitCost;
             itcmcLineCost = quantity * itcmcUnitCost;
             itcmcLoadedCost = 0.00D;
           }
         }
       } else if (isKitBreak()) {
         log.debug(">>>> isKitBreak()");
         Inventory inventoryMC = (Inventory) getSharedInventory(fromstoreloc, siteidMC);
 
         if (inventoryMC != null) {
           log.debug(">>>> inventoryMC != null");
           MboRemote invcostMC = inventoryMC.getInvCostRecord(fromconditioncode);
 
           if (invcostMC != null) {
             log.debug(">>>> invcostMC != null");
             itcmcUnitCost = invcostMC.getDouble("AVGCOST");
             itcmcActualCost = itcmcUnitCost;
             itcmcLineCost = quantity * itcmcUnitCost;
             itcmcLoadedCost = 0.00D;
           }
         }
       } else if (isInvoice() && getDouble("PRORATECOST") != 0.00) {
         log.debug(">>>> isInvoice() && getDouble(\"PRORATECOST\") != 0.00");
         boolean updateInvSetting = getMboServer().getMaxVar().getBoolean("UPDATEINVENTORY", orgId);
 
         if (updateInvSetting) {
           log.debug(">>>> updateInvSetting = true");
           double value = getDouble("LOADEDCOST");
           log.debug(">>>> value: " + Double.toString(value));
           double totalvalue = curService.calculateCurrencyCost(getUserInfo(), currencyCodeFrom, currencyCodeTo, value, exchangeDate, orgId);
           log.debug(">>>> totalvalue: " + Double.toString(totalvalue));
           itcmcUnitCost = 0.00D;
           itcmcActualCost = 0.00D;
           itcmcLineCost = 0.00D;
           itcmcLoadedCost = totalvalue;
         }
       }
 
       log.debug(">>>> itcmcUnitCost: " + Double.toString(itcmcUnitCost));
       log.debug(">>>> itcmcActualCost: " + Double.toString(itcmcActualCost));
       log.debug(">>>> itcmcLineCost: " + Double.toString(itcmcLineCost));
       log.debug(">>>> itcmcLoadedCost: " + Double.toString(itcmcLoadedCost));
 
       setValue("ITCMCUNITCOST", itcmcUnitCost, NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
       setValue("ITCMCACTUALCOST", itcmcActualCost, NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
       setValue("ITCMCLINECOST", itcmcLineCost, NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
       setValue("ITCMCLOADEDCOST", itcmcLoadedCost, NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
 
       log.debug(">>>> MATRECTRANS.ITCMCUNITCOST: " + Double.toString(getDouble("ITCMCUNITCOST")));
       log.debug(">>>> MATRECTRANS.ITCMCACTUALCOST: " + Double.toString(getDouble("ITCMCACTUALCOST")));
       log.debug(">>>> MATRECTRANS.ITCMCLINECOST: " + Double.toString(getDouble("ITCMCLINECOST")));
       log.debug(">>>> MATRECTRANS.ITCMCLOADEDCOST: " + Double.toString(getDouble("ITCMCLOADEDCOST")));
     }
 
     log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:setCurrentValuesMC(siteidMC) >>>>");
   }
 
   /**
    * Este metodo almacena, en los atributos ITCMCFROMOLDBAL y
    * ITCMCFROMOLDAVGCOST respectivamente, el balance y el costo promedio del
    * item del almacen origen antes de que se registre dicha transaccion y se
    * actualice el inventario.
    *
    * @param siteidMC MultiCurrency Site Id
    * @throws MXException
    * @throws RemoteException
    */
   private void setFromOldValuesMC(String siteidMC) throws MXException, RemoteException {
     log.debug("<<<< Entering itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:setFromOldValuesMC(siteidMC) >>>>");
     if (siteidMC != null && isFromStore() && ((isNew() && isNull("STATUS")) || (isModified() && getString("STATUS").equalsIgnoreCase("COMP")))) {
       log.debug(">>>> siteidMC != null && isFromStore() && ((isNew() && isNull(\"STATUS\")) || (isModified() && getString(\"STATUS\").equalsIgnoreCase(\"COMP\")))");
       String frombin = getString("FROMBIN");
       String fromlot = getString("FROMLOT");
       String fromconditioncode = getString("FROMCONDITIONCODE");
       String fromstoreloc = getString("FROMSTORELOC");
       String fromsiteid = getString("FROMSITEID");
 
       log.debug(">>>> siteidMC: " + siteidMC);
       log.debug(">>>> MATRECTRANS.FROMBIN: " + frombin);
       log.debug(">>>> MATRECTRANS.FROMLOT: " + fromlot);
       log.debug(">>>> MATRECTRANS.FROMCONDITIONCODE: " + fromconditioncode);
       log.debug(">>>> MATRECTRANS.FROMSTORELOC: " + fromstoreloc);
       log.debug(">>>> MATRECTRANS.FROMSITEID: " + fromsiteid);
 
       Inventory inventory = (Inventory) getSharedInventory(fromstoreloc, fromsiteid);
       Inventory inventoryMC = (Inventory) getSharedInventory(fromstoreloc, siteidMC);
 
       if (inventory != null && inventoryMC != null) {
         log.debug(">>>> inventory != null && inventoryMC != null");
         InvBalances invbalances = inventory.getInvBalanceRecord(frombin, fromlot, fromconditioncode, fromstoreloc, fromsiteid);
         MboRemote invcostMC = inventoryMC.getInvCostRecord(fromconditioncode);
 
         if (invbalances != null) {
           log.debug(">>>> invbalances != null");
           setValue("ITCMCFROMOLDBAL", invbalances.getCurrentBalance(), NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
           log.debug(">>>> MATRECTRANS.ITCMCFROMOLDBAL: " + Double.toString(getDouble("ITCMCFROMOLDBAL")));
         }
         if (invcostMC != null) {
           log.debug(">>>> invcostMC != null");
           setValue("ITCMCFROMOLDAVGCOST", invcostMC.getDouble("AVGCOST"), NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
           log.debug(">>>> MATRECTRANS.ITCMCFROMOLDAVGCOST: " + Double.toString(getDouble("ITCMCFROMOLDAVGCOST")));
         }
       }
     }
     log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:setFromOldValuesMC(siteidMC) >>>>");
   }
 
   /**
    * Este metodo almacena, en los atributos ITCMCTOOLDBAL y ITCMCTOOLDAVGCOST
    * respectivamente, el balance y el costo promedio del item del almacen
    * destino antes de que se registre dicha transaccion y se actualice el
    * inventario.
    *
    * @param siteidMC MultiCurrency Site Id
    * @throws MXException
    * @throws RemoteException
    */
   private void setToOldValuesMC(String siteidMC) throws MXException, RemoteException {
     log.debug("<<<< Entering itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:setToOldValuesMC(siteidMC) >>>>");
     if (siteidMC != null && isStore() && ((isNew() && isNull("STATUS")) || (isModified() && getString("STATUS").equalsIgnoreCase("COMP")))) {
       log.debug(">>>> siteidMC != null && isStore() && ((isNew() && isNull(\"STATUS\")) || (isModified() && getString(\"STATUS\").equalsIgnoreCase(\"COMP\")))");
       String tobin = getString("TOBIN");
       String tolot = getString("TOLOT");
       String conditioncode = getString("CONDITIONCODE");
       String tostoreloc = getString("TOSTORELOC");
       String siteid = getString("SITEID");
 
       log.debug(">>>> siteidMC: " + siteidMC);
       log.debug(">>>> MATRECTRANS.TOBIN: " + tobin);
       log.debug(">>>> MATRECTRANS.TOLOT: " + tolot);
       log.debug(">>>> MATRECTRANS.CONDITIONCODE: " + conditioncode);
       log.debug(">>>> MATRECTRANS.TOSTORELOC: " + tostoreloc);
       log.debug(">>>> MATRECTRANS.SITEID: " + siteid);
 
       Inventory inventory = (Inventory) getSharedInventory(tostoreloc, siteid);
       Inventory inventoryMC = (Inventory) getSharedInventory(tostoreloc, siteidMC);
 
       if (inventory != null && inventoryMC != null) {
         log.debug(">>>> inventory != null && inventoryMC != null");
         InvBalances invbalances = inventory.getInvBalanceRecord(tobin, tolot, conditioncode, tostoreloc, siteid);
         MboRemote invcostMC = inventoryMC.getInvCostRecord(conditioncode);
 
         if (invbalances != null) {
           log.debug(">>>> invbalances != null");
           setValue("ITCMCTOOLDBAL", invbalances.getCurrentBalance(), NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
           log.debug(">>>> MATRECTRANS.ITCMCTOOLDBAL: " + Double.toString(getDouble("ITCMCTOOLDBAL")));
         }
         if (invcostMC != null) {
           log.debug(">>>> invcostMC != null");
           setValue("ITCMCTOOLDAVGCOST", invcostMC.getDouble("AVGCOST"), NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
           log.debug(">>>> MATRECTRANS.ITCMCTOOLDAVGCOST: " + Double.toString(getDouble("ITCMCTOOLDAVGCOST")));
         }
       }
     }
     log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:setToOldValuesMC(siteidMC) >>>>");
   }
 
   /**
    * Este metodo almacena, en los atributos ITCMCFROMNEWBAL y
    * ITCMCFROMNEWAVGCOST respectivamente, el balance y el costo promedio del
    * item del almacen origen despues de que se registre dicha transaccion y se
    * actualice el inventario.
    *
    * @param siteidMC MultiCurrency Site Id
    * @throws MXException
    * @throws RemoteException
    */
   private void setFromNewValuesMC(String siteidMC) throws MXException, RemoteException {
     log.debug("<<<< Entering itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:setFromNewValuesMC(siteidMC) >>>>");
     if (siteidMC != null && isFromStore() && ((isNew() && isNull("STATUS")) || (isModified() && getString("STATUS").equalsIgnoreCase("COMP")))) {
       log.debug(">>>> siteidMC != null && isFromStore() && ((isNew() && isNull(\"STATUS\")) || (isModified() && getString(\"STATUS\").equalsIgnoreCase(\"COMP\")))");
       String frombin = getString("FROMBIN");
       String fromlot = getString("FROMLOT");
       String fromconditioncode = getString("FROMCONDITIONCODE");
       String fromstoreloc = getString("FROMSTORELOC");
       String fromsiteid = getString("FROMSITEID");
 
       log.debug(">>>> siteidMC: " + siteidMC);
       log.debug(">>>> MATRECTRANS.FROMBIN: " + frombin);
       log.debug(">>>> MATRECTRANS.FROMLOT: " + fromlot);
       log.debug(">>>> MATRECTRANS.FROMCONDITIONCODE: " + fromconditioncode);
       log.debug(">>>> MATRECTRANS.FROMSTORELOC: " + fromstoreloc);
       log.debug(">>>> MATRECTRANS.FROMSITEID: " + fromsiteid);
 
       Inventory inventory = (Inventory) getSharedInventory(fromstoreloc, fromsiteid);
       Inventory inventoryMC = (Inventory) getSharedInventory(fromstoreloc, siteidMC);
 
       if (inventory != null && inventoryMC != null) {
         log.debug(">>>> inventory != null && inventoryMC != null");
         InvBalances invbalances = inventory.getInvBalanceRecord(frombin, fromlot, fromconditioncode, fromstoreloc, fromsiteid);
         MboRemote invcostMC = inventoryMC.getInvCostRecord(fromconditioncode);
 
         if (invbalances != null) {
           log.debug(">>>> invBalances != null");
           setValue("ITCMCFROMNEWBAL", invbalances.getCurrentBalance(), NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
           log.debug(">>>> MATRECTRANS.ITCMCFROMNEWBAL: " + Double.toString(getDouble("ITCMCFROMNEWBAL")));
         }
         if (invcostMC != null) {
           log.debug(">>>> invcostMC != null");
           setValue("ITCMCFROMNEWAVGCOST", invcostMC.getDouble("AVGCOST"), NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
           log.debug(">>>> MATRECTRANS.ITCMCFROMNEWAVGCOST: " + Double.toString(getDouble("ITCMCFROMNEWAVGCOST")));
         }
       }
     }
     log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:setFromNewValuesMC(siteidMC) >>>>");
   }
 
   /**
    * Este metodo almacena, en los atributos ITCMCTONEWBAL y ITCMCTONEWAVGCOST
    * respectivamente, el balance y el costo promedio del item del almacen
    * destino despues de que se registre dicha transaccion y se actualice el
    * inventario.
    *
    * @param siteidMC MultiCurrency Site Id
    * @throws MXException
    * @throws RemoteException
    */
   private void setToNewValuesMC(String siteidMC) throws MXException, RemoteException {
     log.debug("<<<< Entering itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:setToNewValuesMC(siteidMC) >>>>");
     if (siteidMC != null && isStore() && ((isNew() && isNull("STATUS")) || (isModified() && getString("STATUS").equalsIgnoreCase("COMP")))) {
       log.debug(">>>> siteidMC != null && isStore() && ((isNew() && isNull(\"STATUS\")) || (isModified() && getString(\"STATUS\").equalsIgnoreCase(\"COMP\")))");
       String tobin = getString("TOBIN");
       String tolot = getString("TOLOT");
       String conditioncode = getString("CONDITIONCODE");
       String tostoreloc = getString("TOSTORELOC");
       String siteid = getString("SITEID");
 
       log.debug(">>>> siteidMC: " + siteidMC);
       log.debug(">>>> MATRECTRANS.TOBIN: " + tobin);
       log.debug(">>>> MATRECTRANS.TOLOT: " + tolot);
       log.debug(">>>> MATRECTRANS.CONDITIONCODE: " + conditioncode);
       log.debug(">>>> MATRECTRANS.TOSTORELOC: " + tostoreloc);
       log.debug(">>>> MATRECTRANS.SITEID: " + siteid);
 
       Inventory inventory = (Inventory) getSharedInventory(tostoreloc, siteid);
       Inventory inventoryMC = (Inventory) getSharedInventory(tostoreloc, siteidMC);
 
       if (inventory != null && inventoryMC != null) {
         log.debug(">>>> inventory != null && inventoryMC != null");
         InvBalances invbalances = inventory.getInvBalanceRecord(tobin, tolot, conditioncode, tostoreloc, siteid);
         MboRemote invcostMC = inventoryMC.getInvCostRecord(conditioncode);
 
         if (invbalances != null) {
           log.debug(">>>> invbalances != null");
           setValue("ITCMCTONEWBAL", invbalances.getCurrentBalance(), NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
           log.debug(">>>> MATRECTRANS.ITCMCTONEWBAL: " + Double.toString(getDouble("ITCMCTONEWBAL")));
         }
         if (invcostMC != null) {
           log.debug(">>>> invcostMC != null");
           setValue("ITCMCTONEWAVGCOST", invcostMC.getDouble("AVGCOST"), NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
           log.debug(">>>> MATRECTRANS.ITCMCTONEWAVGCOST: " + Double.toString(getDouble("ITCMCTONEWAVGCOST")));
         }
       }
     }
     log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:setToNewValuesMC(siteidMC) >>>>");
   }
 
   /**
    * Este metodo actualiza el costo promedio MC cuando realice alguna
    * transaccion en la MatRecTrans.
    *
    * @param siteidMC MultiCurrency Site Id
    * @throws MXException
    * @throws RemoteException
    */
   private void updateAverageCostMC(String siteidMC) throws MXException, RemoteException {
     log.debug("<<<< Entering itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:updateAverageCostMC(siteidMC) >>>>");
    if (siteidMC != null && ((isNew() && isNull("STATUS")) || (isModified() && getString("STATUS").equalsIgnoreCase("COMP"))) && !isHolding()) {
       log.debug(">>>> siteidMC != null && ((isNew() && isNull(\"STATUS\")) || (isModified() && getString(\"STATUS\").equalsIgnoreCase(\"COMP\")))");
       Inventory toInventory = (Inventory) getSharedInventory(getString("TOSTORELOC"), siteidMC);
 
       if (toInventory == null) {
         log.debug(">>>> toInventory == null");
         InventorySetRemote invSet = (InventorySetRemote) getMboSet("$NEWINV", "INVENTORY", "1>2");
         toInventory = (Inventory) invSet.add();
 
         if (isNull("RECEIVEDUNIT")) {
           log.debug(">>>> isNull(\"RECEIVEDUNIT\")");
           MboRemote poline = getPOLine();
 
           if (poline != null) {
             log.debug(">>>> poline != null");
             toInventory.setValue("ISSUEUNIT", poline.getString("ORDERUNIT"), NOACCESSCHECK);
           } else {
             log.debug(">>>> poline == null");
             SqlFormat sqf1 = new SqlFormat(this, "itemnum=:itemnum and itemsetid=:itemsetid");
             MboSetRemote inventorySet = getMboSet("$INVSET", "INVENTORY", sqf1.format());
 
             if (inventorySet.isEmpty()) {
               log.debug(">>>> inventorySet.isEmpty()");
               Object param[] = {getString("ITEMNUM"), getString("TOSTORELOC")};
               log.debug(">>>> MXApplicationException(\"asset\", \"noInventory\", param)");
               log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:getNewInventoryMC(tostoreloc, siteidMC) >>>>");
               throw new MXApplicationException("asset", "noInventory", param);
             }
 
             MboRemote inventory = inventorySet.getMbo(0);
             String assetIssueUnit = inventory.getString("ISSUEUNIT");
             log.debug(">>>> assetIssueUnit: " + assetIssueUnit);
             toInventory.setValue("ISSUEUNIT", assetIssueUnit, NOACCESSCHECK);
           }
         } else {
           log.debug(">>>> !isNull(\"RECEIVEDUNIT\")");
           String receiveUnit = getString("RECEIVEDUNIT");
           log.debug(">>>> receiveUnit: " + receiveUnit);
           toInventory.setValue("ISSUEUNIT", receiveUnit, NOACCESSCHECK);
         }
 
         toInventory.setValue("ITEMNUM", getString("ITEMNUM"), NOACCESSCHECK);
         toInventory.setPropagateKeyFlag(false);
         toInventory.setValue("SITEID", siteidMC, NOACCESSCHECK);
         toInventory.setPropagateKeyFlag(true);
         toInventory.setValue("LOCATION", getString("TOSTORELOC"), NOACCESSCHECK);
         toInventory.setValue("CONDITIONCODE", getString("CONDITIONCODE"), NOACCESSCHECK);
 
         SqlFormat sqf2 = new SqlFormat(this, "location=:tostoreloc and siteid = :1");
         sqf2.setObject(1, "LOCATIONS", "SITEID", siteidMC);
 
         LocationSetRemote destLocMbo = (LocationSetRemote) getMboSet("$LOCATIONS", "LOCATIONS", sqf2.format());
 
         if (destLocMbo.isEmpty()) {
           log.debug(">>>> destLocMbo.isEmpty()");
           String params[] = {getString("TOSTORELOC"), siteidMC};
           log.debug(">>>> MXApplicationException(\"locations\", \"invalidlocationsite\", param)");
           log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:getNewInventoryMC(tostoreloc, siteidMC) >>>>");
           throw new MXApplicationException("locations", "invalidlocationsite", params);
         }
 
         LocationRemote toLoc = (LocationRemote) destLocMbo.getMbo(0);
 
         if (getTranslator().toInternalString("LINETYPE", getString("LINETYPE")).equals("TOOL")) {
           log.debug(">>>> getTranslator().toInternalString(\"LINETYPE\", getString(\"LINETYPE\")).equals(\"TOOL\")");
           toInventory.setValue("CONTROLACC", toLoc.getString("TOOLCONTROLACC"), NOACCESSCHECK);
         } else {
           log.debug(">>>> !getTranslator().toInternalString(\"LINETYPE\", getString(\"LINETYPE\")).equals(\"TOOL\")");
           toInventory.setValue("CONTROLACC", toLoc.getString("CONTROLACC"), NOACCESSCHECK);
         }
 
         toInventory.setValue("SHRINKAGEACC", toLoc.getString("SHRINKAGEACC"), NOACCESSCHECK);
         toInventory.setValue("INVCOSTADJACC", toLoc.getString("INVCOSTADJACC"), NOACCESSCHECK);
 
         ItemRemote item = (ItemRemote) getMboSet("ITEM").getMbo(0);
 
         if (item != null) {
           log.debug(">>>> item != null");
 
           if (item.isLotted()) {
             log.debug(">>>> item.isLotted()");
             toInventory.setValue("LOTNUM", getString("TOLOT"));
           }
 
           if (item.isConditionEnabled()) {
             log.debug(">>>> item.isConditionEnabled()");
 
             if (isNull("CONDRATE")) {
               log.debug(">>>> isNull(\"CONDRATE\")");
               setValue("CONDRATE", toInventory.getDouble("CONDRATE"), NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
             }
 
             if (getDouble("CONDRATE") < 100.00D) {
               log.debug(">>>> getDouble(\"CONDRATE\") < 100.00D");
               MboRemote itemCond = item.getOneHundredPercent();
 
               if (itemCond != null) {
                 log.debug(">>>> itemCond != null");
                 MboRemote invCost = toInventory.getMboSet("INVCOST").add();
 
                 invCost.setValue("CONDITIONCODE", itemCond.getString("CONDITIONCODE"), NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
                 invCost.setValue("CONDRATE", 100.00D, NOACCESSCHECK | NOVALIDATION_AND_NOACTION);
                 double ratio = getDouble("CONDRATE") / 100.00D;
 
                 if (ratio != 0.0D) {
                   log.debug(">>>> ratio != 0.00D");
                   invCost.setValue("STDCOST", toInventory.getDouble("STDCOST") / ratio, NOACCESSCHECK);
                 } else {
                   log.debug(">>>> ratio == 0.00D");
                   invCost.setValue("STDCOST", getDouble("ITCMCUNITCOST"), NOACCESSCHECK);
                 }
               }
             }
           }
         }
 
         toInventory.setCostType();
         toInventory.setAutoCreateInvBalances(false);
         toInventory.setAutoCreateInvCost(false);
       }
 
       InvCost toInvCost = (InvCost) getInvCostRecordMC(toInventory, siteidMC);
       String costType = toInventory.getCostType();
 
       log.debug(">>>> costType: " + costType);
 
       if (!costType.equals("LIFO") && !costType.equals("FIFO") && toInvCost == null) {
         log.debug(">>>> !costType.equals(\"LIFO\") && !costType.equals(\"FIFO\") && toInvCost == null");
         toInvCost = (InvCost) toInventory.getMboSet("INVCOST").addAtEnd();
 
         if (isNull("CONDITIONCODE") && !getString("CONDITIONCODE").equals("")) {
           log.debug(">>>> isNull(\"CONDITIONCODE\") && !getString(\"CONDITIONCODE\").equals(\"\")");
           toInvCost.setValue("CONDITIONCODE", getString("CONDITIONCODE"), NOACCESSCHECK);
         }
 
         toInvCost.setValue("STDCOST", getDouble("UNITCOST"), NOACCESSCHECK);
       }
 
       Inventory fromInventory = null;
       InvCost fromInvCost = null;
       double quantity = getDouble("QUANTITY");
       String orgId = getString("ORGID");
 
       log.debug(">>>> MATRECTRANS.QUANTITY: " + Double.toString(quantity));
       log.debug(">>>> MATRECTRANS.ORGID: " + orgId);
 
       if (!isNull("FROMSTORELOC")) {
         log.debug(">>>> !isNull(\"FROMSTORELOC\")");
         fromInventory = (Inventory) getSharedInventory(getString("FROMSTORELOC"), siteidMC);
         fromInvCost = (InvCost) getInvCostRecordMC(fromInventory, siteidMC);
       }
 
       if (isReceipt() && !isHolding()) {
         log.debug(">>>> isReceipt() && !isHolding()");
         double totalvalue = getDouble("ITCMCLOADEDCOST");
         log.debug(">>>> totalvalue: " + Double.toString(totalvalue));
         toInventory.updateInventoryAverageCost(quantity, totalvalue, 1.00D, toInvCost);
       } else if (isReturn()) {
         log.debug(">>>> isReturn()");
         double totalvalue = getDouble("ITCMCLOADEDCOST");
         log.debug(">>>> totalvalue: " + Double.toString(totalvalue));
         toInventory.updateInventoryAverageCost(quantity, totalvalue, 1.00D, toInvCost);
       } else if (isVoidReceipt()) {
         log.debug(">>>> isVoidReceipt()");
         double totalvalue = getDouble("ITCMCLOADEDCOST");
         log.debug(">>>> totalvalue: " + Double.toString(totalvalue));
         toInventory.updateInventoryAverageCost(quantity, totalvalue, 1.00D, toInvCost);
       } else if (isTransfer()) {
         log.debug(">>>> isTransfer()");
         double totalvalue = getDouble("ITCMCLOADEDCOST");
         log.debug(">>>> totalvalue: " + Double.toString(totalvalue));
         toInventory.updateInventoryAverageCost(quantity, totalvalue, 1.00D, toInvCost);
       } else if (isShipTransfer()) {
         log.debug(">>>> isShipTransfer()");
         return;
       } else if (isShipCancel()) {
         log.debug(">>>> isShipCancel()");
         return;
       } else if (isShipReceipt() && !isHolding()) {
         log.debug(">>>> isShipReceipt() && !isHolding()");
         double totalvalue = getDouble("ITCMCLOADEDCOST");
         log.debug(">>>> totalvalue: " + Double.toString(totalvalue));
         toInventory.updateInventoryAverageCost(quantity, totalvalue, 1.00D, toInvCost);
       } else if (isShipReturn()) {
         log.debug(">>>> isShipReturn()");
 
         if (fromInventory != null && fromInvCost != null) {
           log.debug("fromInventory != null && fromInvCost != null");
           double totalvalue = getDouble("ITCMCLOADEDCOST");
           log.debug(">>>> totalvalue: " + Double.toString(totalvalue));
 
           toInventory.updateInventoryAverageCost(quantity, totalvalue, 1.00D, toInvCost);
           fromInventory.updateInventoryAverageCost(quantity, (totalvalue * -1.00D), 1.00D, fromInvCost);
         }
       } else if (isVoidShipReceipt()) {
         log.debug(">>>> isVoidShipReceipt()");
 
         if (fromInventory != null && fromInvCost != null) {
           log.debug(">>>> fromInventory != null && fromInvCost != null");
           double totalvalue = getDouble("ITCMCLOADEDCOST");
           log.debug(">>>> totalvalue: " + Double.toString(totalvalue));
 
           toInventory.updateInventoryAverageCost(quantity, totalvalue, 1.00, toInvCost);
           fromInventory.updateInventoryAverageCost(quantity, (totalvalue * -1.00D), 1.00D, fromInvCost);
         }
       } else if (isStageTransfer()) {
         log.debug(">>>> isStageTransfer()");
         double totalvalue = getDouble("ITCMCLOADEDCOST");
         log.debug(">>>> totalvalue: " + Double.toString(totalvalue));
         toInventory.updateInventoryAverageCost(quantity, totalvalue, 1.00D, toInvCost);
       } else if (isKitMake()) {
         log.debug(">>>> isKitMake()");
         double totalvalue = getDouble("ITCMCLINECOST");
         log.debug(">>>> totalvalue: " + Double.toString(totalvalue));
         toInventory.updateInventoryAverageCost(quantity, totalvalue, 1.00D, toInvCost);
       } else if (isKitBreak()) {
         log.debug(">>>> isKitBreak()");
         double totalvalue = getDouble("ITCMCLINECOST");
         log.debug(">>>> totalvalue: " + Double.toString(totalvalue));
         toInventory.updateInventoryAverageCost(quantity, totalvalue, 1.00D, toInvCost);
       } else if (isInvoice() && getDouble("PRORATECOST") != 0.00) {
         log.debug(">>>> isInvoice() && getDouble(\"PRORATECOST\") != 0.00");
         boolean updateInvSetting = getMboServer().getMaxVar().getBoolean("UPDATEINVENTORY", orgId);
 
         if (updateInvSetting) {
           log.debug(">>>> updateInvSetting = true");
           double totalvalue = getDouble("ITCMCLOADEDCOST");
           log.debug(">>>> totalvalue: " + Double.toString(totalvalue));
           toInventory.updateInventoryAverageCost(quantity, totalvalue, 1.00D, toInvCost);
         }
       }
     }
     log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:updateAverageCostMC(siteidMC) >>>>");
   }
 
   /**
    * Este metodo obtiene el InvCost segun el Inventory Destino y el id de la
    * planta multimoneda
    *
    * @param toInventory
    * @param siteidMC
    * @return invCost
    * @throws MXException
    * @throws RemoteException
    */
   private MboRemote getInvCostRecordMC(MboRemote toInventory, String siteidMC) throws MXException, RemoteException {
     log.debug("<<<< Entering itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:getInvCostRecordMC(toInventory, siteidMC) >>>>");
     MboSetRemote invcostSet = toInventory.getMboSet("INVCOST");
 
     for (MboRemote invCost = invcostSet.moveFirst(); invCost != null; invCost = invcostSet.moveNext()) {
       log.debug(">>>> for invcostSet iteration");
       if (invCost.getString("ITEMNUM").equals(getString("ITEMNUM")) && invCost.getString("LOCATION").equals(getString("TOSTORELOC")) && invCost.getString("ITEMSETID").equals(getString("ITEMSETID")) && invCost.getString("CONDITIONCODE").equals(getString("CONDITIONCODE")) && invCost.getString("SITEID").equals(siteidMC)) {
         log.debug(">>>> return invCost;");
         log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:getInvCostRecordMC(toInventory, siteidMC) >>>>");
         return invCost;
       }
     }
 
     log.debug(">>>> return null;");
     log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:getInvCostRecordMC(toInventory, siteidMC) >>>>");
     return null;
   }
 
   /**
    * Este metodo devuelve el siteid multimoneda asociado al siteid origen de la
    * transaccion.
    *
    * @return MultiCurrency Site Id
    * @throws MXException
    * @throws RemoteException
    */
   private String getFromSiteIdMC() throws MXException, RemoteException {
     log.debug("<<<< Entering itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:getFromSiteIdMC() >>>>");
     MboSetRemote siteSet = getMboSet("$SITE", "SITE", "siteid=:fromsiteid and orgid=:orgid");
 
     if (siteSet != null && !siteSet.isEmpty()) {
       log.debug(">>>> siteSet != null && !siteSet.isEmpty()");
       MboRemote site = siteSet.getMbo(0);
 
       if (site != null) {
         log.debug(">>>> site != null");
         String itcmcSiteId = site.getString("ITCMCSITEID");
 
         log.debug(">>>> SITE.SITEID: " + site.getString("SITEID"));
         log.debug(">>>> SITE.ITCMCSITEID: " + itcmcSiteId);
 
         if (itcmcSiteId.equals("")) {
           log.debug(">>>> itcmcSiteId.equals(\"\")");
           log.debug(">>>> return null;");
           log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:getFromSiteIdMC() >>>>");
           return null;
         }
 
         log.debug(">>>> return " + itcmcSiteId + ";");
         log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:getFromSiteIdMC() >>>>");
         return itcmcSiteId;
       }
     }
     log.debug(">>>> return null");
     log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:getFromSiteIdMC() >>>>");
     return null;
   }
 
   /**
    * Este metodo devuelve el siteid multimoneda asociado al siteid destino de la
    * transaccion.
    *
    * @return MultiCurrency Site Id
    * @throws MXException
    * @throws RemoteException
    */
   private String getToSiteIdMC() throws MXException, RemoteException {
     log.debug("<<<< Entering itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:getToSiteIdMC() >>>>");
     MboSetRemote siteSet = getMboSet("$SITE", "SITE", "siteid=:siteid and orgid=:orgid");
 
     if (siteSet != null && !siteSet.isEmpty()) {
       log.debug(">>>> siteSet != null && !siteSet.isEmpty()");
       MboRemote site = siteSet.getMbo(0);
 
       if (site != null) {
         log.debug(">>>> site != null");
         String itcmcSiteId = site.getString("ITCMCSITEID");
 
         log.debug(">>>> SITE.SITEID: " + site.getString("SITEID"));
         log.debug(">>>> SITE.ITCMCSITEID: " + itcmcSiteId);
 
         if (itcmcSiteId.equals("")) {
           log.debug(">>>> itcmcSiteId.equals(\"\")");
           log.debug(">>>> return null;");
           log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:getToSiteIdMC() >>>>");
           return null;
         }
 
         log.debug(">>>> return " + itcmcSiteId + ";");
         log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:getToSiteIdMC() >>>>");
         return itcmcSiteId;
       }
     }
     log.debug(">>>> return null");
     log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:getToSiteIdMC() >>>>");
     return null;
   }
 
   /**
    * returns true if the tostoreloc is a STOREROOM type of location
    *
    * @throws MXException
    * @throws RemoteException
    */
   private boolean isStore() throws MXException, RemoteException {
     log.debug("<<<< Entering itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:isStore() >>>>");
     LocationRemote loc = (LocationRemote) getMboSet("LOCATIONS").getMbo(0);
     if (loc == null) {
       log.debug(">>>> loc == null");
       log.debug(">>>> return false");
       log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:isStore() >>>>");
       return false;
     } else {
       log.debug(">>>> loc != null");
       log.debug(">>>> return " + Boolean.toString(loc.isStore()));
       log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:isStore() >>>>");
       return loc.isStore();
     }
   }
 
   /**
    * returns true if the fromstoreloc is a STOREROOM type of location
    *
    * @throws MXException
    * @throws RemoteException
    */
   private boolean isFromStore() throws MXException, RemoteException {
     log.debug("<<<< Entering itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:isFromStore() >>>>");
     LocationRemote loc = (LocationRemote) getMboSet("FROMLOCATION").getMbo(0);
     if (loc == null) {
       log.debug(">>>> loc == null");
       log.debug(">>>> return false");
       log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:isFromStore() >>>>");
       return false;
     } else {
       log.debug(">>>> loc != null");
       log.debug(">>>> return " + Boolean.toString(loc.isStore()));
       log.debug("<<<< ==> Exiting itc.plusmc.app.inventory.ITCPlusMCMatRecTrans:isFromStore() >>>>");
       return loc.isStore();
     }
   }
 }
