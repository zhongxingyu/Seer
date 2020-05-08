 package org.oki.transmodel.arcgisAddIns;
 
 import java.io.IOException;
 
 import javax.swing.JOptionPane;
 
 import com.esri.arcgis.addins.desktop.Button;
 import com.esri.arcgis.arcmapui.IMxDocument;
 import com.esri.arcgis.carto.FeatureLayer;
 import com.esri.arcgis.carto.IFeatureSelection;
 import com.esri.arcgis.carto.IMap;
 import com.esri.arcgis.carto.Map;
 import com.esri.arcgis.framework.IApplication;
 import com.esri.arcgis.geodatabase.IEnumIDs;
 import com.esri.arcgis.geodatabase.IRow;
 import com.esri.arcgis.geodatabase.ISelectionSet;
 import com.esri.arcgis.geodatabase.ITable;
 import com.esri.arcgis.interop.AutomationException;
 
 public class flipOriginDestination extends Button {
 	private IApplication app;
 	@Override
 	public void onClick() throws IOException, AutomationException{
 		try{
 			IMxDocument mxDoc = new com.esri.arcgis.arcmapui.IMxDocumentProxy (app.getDocument());
 			IMap focusMap = mxDoc.getFocusMap();
 			int selectedCount=focusMap.getSelectionCount();
 			if(selectedCount>0){
 				for(int x=0;x<focusMap.getLayerCount();x++){
					if(focusMap.getLayer(x).getName().equals("Origin Locations")){
						FeatureLayer layer=(FeatureLayer) focusMap.getLayer(x);
 						IFeatureSelection featSel=layer;
 						ISelectionSet selSet=featSel.getSelectionSet();
 						ITable table=selSet.getTarget();
 						int originXField=table.findField("OXCORD");
 						int originYField=table.findField("OYCORD");
 						int oNameField=table.findField("ONAME");
 						int oGeocodeField=table.findField("OGeocode");
 						int oAddressField=table.findField("OADDR");
 						int oCrossStreet1Field=table.findField("OXSTRT1");
 						int oCrossStreet2Field=table.findField("OXSTRT2");
 						int oCityField=table.findField("OCITY");
 						int oStateField=table.findField("OSTATE");
 						int oZipField=table.findField("OZIP");
 						int oOAVStatusField=table.findField("OAV_STAT");
 						int oOAVAddressField=table.findField("OAV_ADD");
 						int oOAVZoneField=table.findField("OAV_ZONE");
 						
 						int destXField=table.findField("DXCORD");
 						int destYField=table.findField("DYCORD");
 						int dNameField=table.findField("DNAME");
 						int dGeocodeField=table.findField("DGeocode");
 						int dAddressField=table.findField("DADDR");
 						int dCrossStreet1Field=table.findField("DXSTRT1");
 						int dCrossStreet2Field=table.findField("DXSTRT2");
 						int dCityField=table.findField("DCITY");
 						int dStateField=table.findField("DSTATE");
 						int dZipField=table.findField("DZIP");
 						int dDAVStatusField=table.findField("DAV_STAT");
 						int dDAVAddressField=table.findField("DAV_ADD");
 						int dDAVZoneField=table.findField("DAV_ZONE");
 						
 						IEnumIDs selIds=selSet.getIDs();
 						if(selSet.getCount()>1){
 							int reply=JOptionPane.showConfirmDialog(null, "This will flip the origin/destination locations for ALL SELECTED items.  Is this what you want?","Confirm",JOptionPane.YES_NO_OPTION);
 							if(reply==JOptionPane.NO_OPTION){
 								return;
 							}
 						}
 						int iId=selIds.next();
 						while(iId>0){
 							double originX=0;
 							double originY=0;
 							double destX=0;
 							double destY=0;
 							Object oName, oGeocode, oAddress, oCrossStreet1, oCrossStreet2, oCity, oState, oZip, oAVStatus, oAVAddress, oAVZone;
 							IRow row=table.getRow(iId);
 							originX=(Double) row.getValue(originXField);
 							originY=(Double) row.getValue(originYField);
 							destX=(Double) row.getValue(destXField);
 							destY=(Double) row.getValue(destYField);
 							
 							oName=row.getValue(oNameField);
 							oGeocode=row.getValue(oGeocodeField);
 							oAddress=row.getValue(oAddressField);
 							oCrossStreet1=row.getValue(oCrossStreet1Field);
 							oCrossStreet2=row.getValue(oCrossStreet2Field);
 							oCity=row.getValue(oCityField);
 							oState=row.getValue(oStateField);
 							oZip=row.getValue(oZipField);
 							oAVStatus=row.getValue(oOAVStatusField);
 							oAVAddress=row.getValue(oOAVAddressField);
 							oAVZone=row.getValue(oOAVZoneField);
 							
 							row.setValue(oNameField, row.getValue(dNameField));
 							row.setValue(oGeocodeField, row.getValue(dGeocodeField));
 							row.setValue(oAddressField, row.getValue(dAddressField));
 							row.setValue(oCrossStreet1Field, row.getValue(dCrossStreet1Field));
 							row.setValue(oCrossStreet2Field, row.getValue(dCrossStreet2Field));
 							row.setValue(oCityField, row.getValue(dCityField));
 							row.setValue(oStateField, row.getValue(dStateField));
 							row.setValue(oZipField, row.getValue(dZipField));
 							row.setValue(oOAVStatusField, row.getValue(dDAVStatusField));
 							row.setValue(oOAVAddressField, row.getValue(dDAVAddressField));
 							row.setValue(oOAVZoneField, row.getValue(dDAVZoneField));
 							
 							row.setValue(dNameField, oName);
 							row.setValue(dGeocodeField, oGeocode);
 							row.setValue(dAddressField, oAddress);
 							row.setValue(dCrossStreet1Field, oCrossStreet1);
 							row.setValue(dCrossStreet2Field, oCrossStreet2);
 							row.setValue(dCityField, oCity);
 							row.setValue(dStateField, oState);
 							row.setValue(dZipField, oZip);
 							row.setValue(dDAVStatusField, oAVStatus);
 							row.setValue(dDAVAddressField, oAVAddress);
 							row.setValue(dDAVZoneField, oAVZone);
 
 							row.setValue(destXField, originX);
 							row.setValue(destYField, originY);
 							row.setValue(originXField, destX);
 							row.setValue(originYField, destY);
 							row.store();
 							iId=selIds.next();
 						}
 					}
 				}
 			}
 			Map focusMap2=(Map) focusMap;
 			focusMap2.refresh();
 		}
 		catch(Exception e){
 							System.out.println(e.getMessage());
 		}			
 						
 	}
 	@Override
 	public void init(IApplication app) throws IOException, AutomationException {
 		this.app = app;
 		super.init(app);
 	}
 }
