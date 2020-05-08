 package es.udc.cartolab.gvsig.fonsagua.config;
 
 import es.udc.cartolab.gvsig.epanet.config.JunctionFieldNames;
 import es.udc.cartolab.gvsig.epanet.config.LayerNames;
 import es.udc.cartolab.gvsig.epanet.config.PipeFieldNames;
 import es.udc.cartolab.gvsig.epanet.config.Preferences;
 import es.udc.cartolab.gvsig.epanet.config.PumpFieldNames;
 import es.udc.cartolab.gvsig.epanet.config.ReservoirFieldNames;
 import es.udc.cartolab.gvsig.epanet.config.TankFieldNames;
 import es.udc.cartolab.gvsig.epanet.config.ValveFieldNames;
 import es.udc.cartolab.gvsig.fonsagua.OpenAlternativeExtension;
 
 /*
  * fpuga: Epanet Configuration classes should be threaten in another way.
  * XXNames should be immutable objects, created by means of factory or static 
  * factory methods 
  * In fact this implies a mess playing with initilize and postinitialize methods
  * on the CADExtensiones of gvsig-epanet
  */
 public class EpanetConfiguration {
 
     public void setConfig() {
 	setJunctionFieldNames();
 	setTankFieldNames();
 	setReservoirFieldNames();
 	setPipeFieldNames();
 	setPumpFieldNames();
 	setValveFieldNames();
 	setLayerNames();
 	OpenAlternativeExtension.setValidAlternative(false);
     }
 
     private void setLayerNames() {
 	LayerNames layerNames = new LayerNames();
 	layerNames.setJunctions("alt_conexiones");
 	layerNames.setPipes("alt_tuberias");
 	layerNames.setPumps("alt_bombeos");
	layerNames.setReservoirs("alt_embalses");
	layerNames.setSources("alt_fuentes");
 	layerNames.setTanks("alt_depositos");
 	layerNames.setValves("alt_valvulas");
 	Preferences.setLayerNames(layerNames);
     }
 
     private void setValveFieldNames() {
 	ValveFieldNames names = new ValveFieldNames();
 	names.setElevation("cota");
 	names.setDiameter("diametro");
 	names.setSetting("consigna");
 	names.setFlow("caudal");
 	names.setVelocity("velocidad");
 	names.setUnitHeadLoss("perdidas");
 	names.setFrictionFactor(null);
 	Preferences.setValveFieldNames(names);
     }
 
     private void setPumpFieldNames() {
 	PumpFieldNames names = new PumpFieldNames();
 	names.setElevation("cota");
 	names.setValue("potencia");
 	names.setFlow("caudal");
 	names.setVelocity("altura");
 	names.setUnitHeadLoss(null);
 	names.setFrictionFactor(null);
 	Preferences.setPumpFieldNames(names);
     }
 
     private void setPipeFieldNames() {
 	PipeFieldNames names = new PipeFieldNames();
 	names.setDiameter("diametro");
 	names.setRoughness("rugosidad");
 	names.setFlow("caudal");
 	names.setVelocity("velocidad");
 	names.setUnitHeadLoss("perdida_carga");
 	names.setFrictionFactor("factor_friccion");
 	Preferences.setPipeFieldNames(names);
     }
 
     private void setReservoirFieldNames() {
 	ReservoirFieldNames names = new ReservoirFieldNames();
 	names.setTotalHead("altura");
 	names.setPressure(null);
 	names.setHead("altura_total");
 	names.setDemand("caudal");
 	Preferences.setReservoirFieldNames(names);
     }
 
     private void setTankFieldNames() {
 	TankFieldNames names = new TankFieldNames();
 	names.setElevation("cota");
 	names.setInitLevel("nivel_inicial");
 	names.setMinLevel("nivel_minimo");
 	names.setMaxLevel("nivel_maximo");
 	names.setDiameter("diametro");
 	names.setPressure("presion");
 	names.setHead("altura_total");
 	names.setDemand("q_neto_entrante");
 	Preferences.setTankFieldNames(names);
     }
 
     private void setJunctionFieldNames() {
 	JunctionFieldNames names = new JunctionFieldNames();
 	names.setElevation("cota");
 	names.setBaseDemand("demanda");//
 	names.setPressure("presion");
 	names.setHead("altura_total");
 	names.setDemand(null);
 	Preferences.setJunctionFieldNames(names);
     }
 }
