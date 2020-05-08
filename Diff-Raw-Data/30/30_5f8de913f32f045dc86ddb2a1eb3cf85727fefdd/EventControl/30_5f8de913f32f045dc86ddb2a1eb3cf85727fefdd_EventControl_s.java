 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.isolux.control;
 
 import com.isolux.bo.*;
 import com.isolux.dao.Conversion;
 import com.isolux.dao.Utils;
 import com.isolux.dao.jmodbus.EventoDAOJmodbus;
 import com.isolux.dao.properties.PropHandler;
 import com.isolux.utils.Validacion;
 import com.isolux.view.PpalView;
 import com.isolux.view.ViewUtils;
 import java.math.BigInteger;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.ListModel;
 import javax.swing.text.Position;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreePath;
 
 /**
  *
  * @author Juan Diego Toro
  */
 public class EventControl implements ElementoControl_Interface {
 
    
     @Override
     public void saveElement(PpalView ppalView) {
         boolean connectionStatus = true; //DAOJamod.testConnection(ppalView.getIp(), ppalView.getPort());
         new GeneralControl().updateConnectionStatus(connectionStatus, ppalView);
         if (connectionStatus) {
             ppalView.getStatusLabel().setText("Guardando evento");
             //event number
             boolean isUpdate = !ppalView.getjLabel64().getText().equals("#");
 
             int eventNumber = ppalView.getjLabel64().getText().equals("#") ? Integer.parseInt(ppalView.getEventoNum_jComboBox().getSelectedItem().toString()) : Integer.parseInt(ppalView.getjLabel64().getText());
 //            int eventNumber = ppalView.getjLabel64().getText().equals("#") ? PropHandler.getEventNumber() : Integer.parseInt(ppalView.getjLabel64().getText());
 
             String name = ppalView.getNombreevento_jTextField().getText();
             int porFechaODias = ppalView.getPorDiasEvento_jCheckBox().isSelected() ? 2 : 1;
 
             int dia = 0;
             int mes = 0;
             int anho = 0;
             int hora = 0;
             int minuto = 0;
             int diaYrepetir = 0;
             int[] nivelBalasto = new int[Integer.parseInt(PropHandler.getProperty("balast.max.number"))];
             int[] balastosAfectados = new int[Integer.parseInt(PropHandler.getProperty("balast.max.number"))];
             int[] nivelGrupo = new int[Integer.parseInt(PropHandler.getProperty("group.max.number"))];
             int[] gruposAfectados = new int[Integer.parseInt(PropHandler.getProperty("group.max.number"))];
             int[] escenasAfectadas = new int[Integer.parseInt(PropHandler.getProperty("scene.max.number"))];
             int tipoSalida = 0;
             if (ppalView.getselBalastosEvento_jCheckBox().isSelected()) {
                 tipoSalida = 1;
             } else if (ppalView.getSelGruposEntradas_jCheckBox().isSelected()) {
                 tipoSalida = 2;
             } else if (ppalView.getjCheckBox15().isSelected()) {
                 tipoSalida = 3;
             }
 
             if (ppalView.getPorDiasEvento_jCheckBox().isSelected()) { //Por dias
                 int[] dias = new int[16];
                 dias[0] = ppalView.getDomingo_jCheckBox().isSelected() ? 1 : 0;//comienza la semana con domingo y no con lunes
                 dias[1] = ppalView.getLunes_jCheckBox().isSelected() ? 1 : 0;
                 dias[2] = ppalView.getMartes_jCheckBox().isSelected() ? 1 : 0;
                 dias[3] = ppalView.getMiercoles_jCheckBox().isSelected() ? 1 : 0;
                 dias[4] = ppalView.getJuevesjCheckBox().isSelected() ? 1 : 0;
                 dias[5] = ppalView.getViernes_jCheckBox().isSelected() ? 1 : 0;
                 dias[6] = ppalView.getSabado_jCheckBox().isSelected() ? 1 : 0;
                 dias[7] = 0;
                 dias[8] = 0;
                 dias[9] = 0;
                 dias[10] = 0;
                 dias[11] = 0;
                 dias[12] = 0;
                 dias[13] = 0;
                 dias[14] = 0;
                 dias[15] = 0;
 
                 String selectedDays = new String();
                 for (int diaSelected : dias) {
                     selectedDays = String.valueOf(diaSelected) + selectedDays;
                 }
 
                 diaYrepetir = new BigInteger(selectedDays, 2).intValue();
 
             } else { //por fecha.
                 String[] date = new SimpleDateFormat("dd/MM/yyyy").format(ppalView.getjDateChooser2().getDate()).split("/");
 
                 dia = Integer.parseInt(date[0]);
                 mes = Integer.parseInt(date[1]);
                 anho = Integer.parseInt(date[2]);
 
                String[] time = ppalView.getjFormattedTextField2().getText().split(":");
                 hora = Integer.parseInt(time[0]);
                 minuto = Integer.parseInt(time[1]);
             }
 
             //Afected elements.
             ppalView.setEventOutType(ppalView.getselBalastosEvento_jCheckBox().isSelected() ? 1 : ppalView.getSelGruposEntradas_jCheckBox().isSelected() ? 2 : ppalView.getjCheckBox15().isSelected() ? 3 : 0);
             balastosAfectados = ppalView.getEventOutType() == ViewUtils.getIntProperty("in.out.type.balast") ? getSelectedEventItems(ppalView)
                     : new int[Integer.parseInt(PropHandler.getProperty("balast.max.number"))];
             gruposAfectados = ppalView.getEventOutType() == ViewUtils.getIntProperty("in.out.type.group") ? getSelectedEventItems(ppalView)
                     : new int[Integer.parseInt(PropHandler.getProperty("group.max.number"))];
             escenasAfectadas = ppalView.getEventOutType() == ViewUtils.getIntProperty("in.out.type.scene") ? getSelectedEventItems(ppalView)
                     : new int[Integer.parseInt(PropHandler.getProperty("scene.max.number"))];
 
             //Balasts/group values.
             if (ppalView.getselBalastosEvento_jCheckBox().isSelected()) {
                 ListModel selectedElements = ppalView.getjList13().getModel();
                 for (int i = 0; i < selectedElements.getSize(); i++) {
                     try {
                         String item = selectedElements.getElementAt(i).toString();
                         String[] level = item.split(": ");
                         nivelBalasto[Integer.parseInt(item.split(" - ")[0])] = Integer.parseInt(level[level.length - 1]);
                     } catch (Exception e) {
                     }
                 }
             }
             if (ppalView.getSelGruposEntradas_jCheckBox().isSelected()) {
                 ListModel selectedElements = ppalView.getjList13().getModel();
                 for (int i = 0; i < selectedElements.getSize(); i++) {
                     try {
                         String item = selectedElements.getElementAt(i).toString();
                         String[] level = item.split(": ");
                         nivelGrupo[Integer.parseInt(item.split(" - ")[0])] = Integer.parseInt(level[level.length - 1]);
                     } catch (Exception e) {
                     }
                 }
             }
 
             Evento newEvent = new Evento(
                     eventNumber,
                     name,
                     porFechaODias,
                     dia,
                     mes,
                     anho,
                     hora,
                     minuto,
                     diaYrepetir,
                     nivelBalasto,
                     balastosAfectados,
                     nivelGrupo,
                     gruposAfectados,
                     escenasAfectadas,
                     tipoSalida);
 
 
             //Saves the balast remotelly
             EventoDAOJmodbus dao = new EventoDAOJmodbus(ppalView.getDao());
             boolean resultado = dao.saveElement(newEvent);
 
             if (isUpdate) {
                 //Update event locally.
                 ppalView.getEvents().remove(String.valueOf(newEvent.getNumeroEvento()));
                 ppalView.getEvents().put(String.valueOf(newEvent.getNumeroEvento()), newEvent);
 
                 if (resultado) {
                     ppalView.getStatusLabel().setText("Evento actualizado.");
                 } else {
                     ppalView.getStatusLabel().setText("Ocurrió un error guardando el evento.");
                 }
 
                 //Update name in the tree.
                 String eventText = PropHandler.getProperty("events.menu.name");
 
                 TreePath path = ppalView.getArbol_jTree().getNextMatch(eventText, 0, Position.Bias.Forward);
                 MutableTreeNode eventNode = (MutableTreeNode) path.getLastPathComponent();
                 Enumeration events = eventNode.children();
 
                 while (events.hasMoreElements()) {
                     DefaultMutableTreeNode updatedScene = (DefaultMutableTreeNode) events.nextElement();
                     String[] numberName = updatedScene.getUserObject().toString().split(" - ");
                     if (numberName[0].equals(String.valueOf(newEvent.getNumeroEvento()))) {
                         updatedScene.setUserObject(numberName[0] + " - " + newEvent.getNombre());
                     }
                 }
 
             } else {
                 if (new BalastosControl().validateBalastoForm()) {
                     ppalView.getEvents().put(String.valueOf(newEvent.getNumeroEvento()), newEvent);
 
                     if (resultado) {
                         ppalView.getStatusLabel().setText("Evento guardado");
                     } else {
                         ppalView.getStatusLabel().setText("Ocurrió un error guardando el evento.");
                     }
 
                     //Update name in the tree.
                     String eventText = PropHandler.getProperty("events.menu.name");
 
                     //Show balast in tree
                     DefaultTreeModel model = (DefaultTreeModel) ppalView.getArbol_jTree().getModel();
                     TreePath path = ppalView.getArbol_jTree().getNextMatch(eventText, 0, Position.Bias.Forward);
                     MutableTreeNode eventNode = (MutableTreeNode) path.getLastPathComponent();
                     DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(String.valueOf(newEvent.getNumeroEvento()) + " - " + newEvent.getNombre());
                     model.insertNodeInto(newNode, eventNode, eventNode.getChildCount());
 
                     ppalView.getjLabel63().setText(String.valueOf(eventNumber));
                 }
             }
         }
         refrescarVista(ppalView);
     }
 
    
     @Override
     public void deleteElement(PpalView ppalView) {
         if (ppalView.getSelectedEventNumber() != null && !ppalView.getSelectedEventNumber().equals("")) {
             DefaultMutableTreeNode nodeToDelete = (DefaultMutableTreeNode) ppalView.getArbol_jTree().getLastSelectedPathComponent();
             DefaultTreeModel treeModel = (DefaultTreeModel) ppalView.getArbol_jTree().getModel();
             EventoDAOJmodbus dao = new EventoDAOJmodbus(ppalView.getDao());
             dao.deleteElement(ppalView.getSelectedEventNumber());
             treeModel.removeNodeFromParent(nodeToDelete);
             ppalView.getEvents().remove(ppalView.getSelectedEventNumber());
             cleanView(ppalView);
         } else {
             Validacion.showAlertMessage("Debe seleccionar un evento primero!");
         }
     }
 
    
     @Override
     public void showSelectedElement(String eventNumber, PpalView ppalView) {
         Evento selectedEvent = ppalView.getEvents().get(eventNumber);
         HashMap<String, Balasto> balasts = ppalView.getBalasts();
 
         ppalView.getNombreevento_jTextField().setText(selectedEvent.getNombre());
         ppalView.getjLabel41().setText(String.valueOf(selectedEvent.getNumeroEvento()));
 
         boolean porFechaODias = selectedEvent.getPorFechaODias() == 1 ? false : true;
         ppalView.getPorDiasEvento_jCheckBox().setSelected(porFechaODias);
         ppalView.getjLabel64().setText(eventNumber);
 
         if (porFechaODias) { //true: pordias, false: por fecha.
             this.selectByDays(ppalView);//habilita los checkbox de dias
             //Seleccionamos los días que tiene el evento
             Integer diaYrepetir = selectedEvent.getDiaYrepetir();
             String by = (String) Integer.toBinaryString(diaYrepetir);
             String ceros = Utils.getCeros(by, 7);
             StringBuffer byn = new StringBuffer(ceros);
 
             selectAffectedDays(byn, Conversion.LITTLEENDIAN, ppalView);
 
         } else {
             Date date = new Date();
             try {
 //            date = new SimpleDateFormat("dd/MM/yyyy").parse(selectedEvent.getDia() + "/" + selectedEvent.getMes() + "/" + selectedEvent.getAnho());
                 date = new SimpleDateFormat("dd/MM/yyyy").parse(selectedEvent.getDia() + "/" + selectedEvent.getMes() + "/" + (selectedEvent.getAnho()));
             } catch (ParseException ex) {
                Logger.getLogger(PpalView.class.getName()).log(Level.SEVERE, null, ex);
             }
             ppalView.getjDateChooser2().setDate(date);
             ppalView.getjFormattedTextField2().setText(selectedEvent.getHora() + ":" + selectedEvent.getMinuto());
         }
 
         //Show in type.
         if (selectedEvent.getTipoSalida() == 1) {
 
             //BALASTS = 1
             //Afected balasts
             DefaultListModel sceneBalastsL = new DefaultListModel();
             int[] selectedBalasts = selectedEvent.getBalastosAfectados();
             int[] selectedBalastsLevels = selectedEvent.getNivelBalasto();
             ArrayList sel = new ArrayList();
             for (int i = 0; i < selectedBalasts.length; i++) {
                 if (selectedBalasts[i] == 1) {
                     new BalastosControl().readElements(ppalView);
                     Balasto sce = balasts.get(String.valueOf(i));
                     sceneBalastsL.addElement(sce.getBalastNumber() + " - " + sce.getName() + ": " + selectedBalastsLevels[i]);
                     sel.add(String.valueOf(i));
                 }
             }
             ppalView.getjList13().setModel(sceneBalastsL);
 
             //Available balasts
             DefaultListModel modelo = new DefaultListModel();
             ArrayList<String> addedBalasts = PropHandler.getAddedBalasts(ppalView.getDao());
             for (String balastNumber : addedBalasts) {
                 if (!sel.contains(balastNumber)) {
                     Balasto balasto = balasts.get(balastNumber);
                     modelo.addElement(balasto.getBalastNumber() + " - " + balasto.getName());
                 }
             }
             ppalView.getselBalastosEvento_jCheckBox().setSelected(true);
             ppalView.getSelGruposEntradas_jCheckBox().setEnabled(false);
             ppalView.getjCheckBox15().setEnabled(false);
             ppalView.getjList12().setModel(modelo);
         } else if (selectedEvent.getTipoSalida() == 2) {
             //GROUPS = 2
             //Afected groups
             DefaultListModel sceneGroupsL = new DefaultListModel();
             int[] selectedGroups = selectedEvent.getGruposAfectados();
             int[] selectedGroupLevels = selectedEvent.getNivelGrupo();
             ArrayList selGroups = new ArrayList();
             for (int i = 0; i < selectedGroups.length; i++) {
                 if (selectedGroups[i] == 1) {
                     new GrupoControl().readElements(ppalView);
                     Grupo group = ppalView.getGroups().get(String.valueOf(i));
                     sceneGroupsL.addElement(group.getGroupNumber() + " - " + group.getName() + ": " + selectedGroupLevels[i]);
                     selGroups.add(String.valueOf(i));
                 }
             }
             ppalView.getjList13().setModel(sceneGroupsL);
 
             //Available groups
             DefaultListModel modelGroups = new DefaultListModel();
             ArrayList<String> addedGroups = PropHandler.getAddedGroups(ppalView.getDao());
             for (String groupNumber : addedGroups) {
                 if (!selGroups.contains(groupNumber)) {
                     Grupo group = ppalView.getGroups().get(groupNumber);
                     modelGroups.addElement(group.getGroupNumber() + " - " + group.getName());
                 }
             }
             ppalView.getSelGruposEntradas_jCheckBox().setSelected(true);
             ppalView.getselBalastosEvento_jCheckBox().setEnabled(false);
             ppalView.getjCheckBox15().setEnabled(false);
             ppalView.getjList12().setModel(modelGroups);
 
         } else if (selectedEvent.getTipoSalida() == 3) {
             //SCENES = 3
             //Afected scenes
             DefaultListModel sceneScenesL = new DefaultListModel();
             int[] selectedScenes = selectedEvent.getEscenasAfectadas();
             ArrayList selScenes = new ArrayList();
             for (int i = 0; i < selectedScenes.length; i++) {
                 if (selectedScenes[i] == 1) {
                     new EscenaControl().readElements(ppalView);
                     Escena sce = ppalView.getScenes().get(String.valueOf(i));
                     sceneScenesL.addElement(sce.getNumeroEscena() + " - " + sce.getNombre());
                     selScenes.add(String.valueOf(i));
                 }
             }
             ppalView.getjList13().setModel(sceneScenesL);
 
             //Available scenes
             DefaultListModel modelScenes = new DefaultListModel();
             ArrayList<String> addedScenes = PropHandler.getAddedScenes(ppalView.getDao());
             for (String sceneNumber : addedScenes) {
                 if (!selScenes.contains(sceneNumber)) {
                     Escena escena = ppalView.getScenes().get(sceneNumber);
                     modelScenes.addElement(escena.getNumeroEscena() + " - " + escena.getNombre());
                 }
             }
             ppalView.getjCheckBox15().setSelected(true);
             ppalView.getSelGruposEntradas_jCheckBox().setEnabled(false);
             ppalView.getselBalastosEvento_jCheckBox().setEnabled(false);
             ppalView.getjList12().setModel(modelScenes);
         }
 
     }
 
     /**
      * Show the available events in the tree.
      *
      * @param ppalView
      */
     public void showAvailableEvents(PpalView ppalView) {
         readElements(ppalView);
         DefaultListModel modelo = new DefaultListModel();
 
         ArrayList<String> addedEvents = PropHandler.getAddedEvents(ppalView.getDao());
         for (String eventNumber : addedEvents) {
             Evento event = ppalView.getEvents().get(eventNumber);
             modelo.addElement(event.getNumeroEvento() + " - " + event.getNombre());
         }
         ppalView.getjList2().setModel(modelo);
         ppalView.getjList2().setLayoutOrientation(JList.VERTICAL_WRAP);
     }
 
    
     @Override
     public void readElements(PpalView ppalView) {
         if (ppalView.getEvents() == null || ppalView.getEvents().size() == 0) {
             EventoDAOJmodbus dao = new EventoDAOJmodbus(ppalView.getDao());
             ppalView.setEvents(new HashMap<String, Evento>());
 
             //Added in numbers.
             ArrayList<String> addedEvents = PropHandler.getAddedEvents(ppalView.getDao());
 
             //In.
             for (String eventNumber : addedEvents) {
                 ppalView.getEvents().put(eventNumber, dao.readElement(Integer.parseInt(eventNumber)));
             }
         }
     }
 
     
     @Override
     public void cleanView(PpalView ppalView) {
         ppalView.getNombreevento_jTextField().setText("event");
         ppalView.getjLabel64().setText("#");
         ppalView.getPorDiasEvento_jCheckBox().setSelected(false);
         ppalView.getjDateChooser2().setDate(null);
         ppalView.getjFormattedTextField2().setText("00:00");
         ppalView.getjTextField25().setText("0");
         ppalView.getjLabel23().setText("#");
         ppalView.getselBalastosEvento_jCheckBox().setSelected(false);
         ppalView.getSelGruposEntradas_jCheckBox().setSelected(false);
         ppalView.getjCheckBox15().setSelected(false);
         ppalView.getselBalastosEvento_jCheckBox().setEnabled(true);
         ppalView.getSelGruposEntradas_jCheckBox().setEnabled(true);
         ppalView.getjCheckBox15().setEnabled(true);
 
         DefaultListModel model = new DefaultListModel();
         ppalView.getjList12().setModel(model);
         ppalView.getjList13().setModel(model);
 
     }
 
     /**
      * Filter the used Events (add existing events to the menu).
      *
      * @param ppalView Vista que se está controlando
      */
     @Override
     public void filterAddedElements(PpalView ppalView) {
         if (!ppalView.getEventStauts()) {
             ppalView.getStatusLabel().setText("Leyendo eventos de la tarjeta...");
             readElements(ppalView);
             int startRow = 0;
             String prefix = PropHandler.getProperty("events.menu.name");
             TreePath path = ppalView.getArbol_jTree().getNextMatch(prefix, startRow, Position.Bias.Forward);
 
             DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
             ArrayList<String> addedEvents = PropHandler.getAddedEvents(ppalView.getDao());
             DefaultTreeModel model = (DefaultTreeModel) ppalView.getArbol_jTree().getModel();
 
             //Remove the used balast numbers from the list and add them to the menu.
             for (String eventNumber : addedEvents) {
                 Evento event = ppalView.getEvents().get(eventNumber);
                 DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(eventNumber + " - " + event.getNombre());
                 model.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount());
             }
             ppalView.setEventStauts(true);
             ppalView.getStatusLabel().setText("Eventos leidos.");
         }
     }
 
     /**
      * Sets the in type.
      *
      * @param node
      */
     public void eventSetType(String node, PpalView ppalView) {
         String btns = PropHandler.getProperty("btns.menu.name");
         String ftcld = PropHandler.getProperty("ftcl.menu.name");
         String sensor = PropHandler.getProperty("sensors.menu.name");
 
         if (node.equals(btns)) {
             ppalView.setInType(Integer.parseInt(PropHandler.getProperty("in.type.btns")));
         }
         if (node.equals(ftcld)) {
             ppalView.setInType(Integer.parseInt(PropHandler.getProperty("in.type.ftcld")));
         }
         if (node.equals(sensor)) {
             ppalView.setInType(Integer.parseInt(PropHandler.getProperty("in.type.sensor")));
         }
     }
 
     /**
      * Get a string with the selected items.
      */
     private int[] getSelectedEventItems(PpalView ppalView) {
         String selected = new String();
         int arrayLength = 0;
         JList selectedList = ppalView.getjList13();
 
         //Gets the array length
         if (ppalView.getEventOutType() == ViewUtils.getIntProperty("in.out.type.balast")) { //balasts
             arrayLength = ViewUtils.getIntProperty("balast.max.number");
         } else if (ppalView.getEventOutType() == ViewUtils.getIntProperty("in.out.type.group")) { //groups
             arrayLength = ViewUtils.getIntProperty("group.max.number");
         } else if (ppalView.getEventOutType() == ViewUtils.getIntProperty("in.out.type.scene")) { //scenes
             arrayLength = ViewUtils.getIntProperty("scene.max.number");
         }
 
         //Array with selected items.
         int[] selectedItems = new int[arrayLength];
 
         //fill selected items array
         for (int i = 0; i < selectedList.getModel().getSize(); i++) {
             selected += selectedList.getModel().getElementAt(i).toString().split(" - ")[0] + ",";
         }
         String[] selectedIdx = selected.split(",");
         for (int i = 0; i < selectedIdx.length; i++) {
             selectedItems[Integer.parseInt(selectedIdx[i])] = 1;
         }
 
         return selectedItems;
     }
 
     /**
      * Gets the selection JList
      *
      * @return
      */
     private JList getEventSelectionList(PpalView ppalView) {
         JList selectedList = new JList();
         if (ppalView.getInType() == ViewUtils.getIntProperty("in.type.btns")) { //Botonera
             selectedList = ppalView.getjList15();
         }
         if (ppalView.getInType() == ViewUtils.getIntProperty("in.type.ftcld")) { //Fotocelda
             selectedList = ppalView.getjList9();
         }
         if (ppalView.getInType() == ViewUtils.getIntProperty("in.type.sensor")) { //Sensor
             selectedList = ppalView.getjList17();
         }
 
         return selectedList;
     }
 
     /**
      * Show the in items. Muestra los elementos que van a ser afectados como los
      * balastos afectados, grupos afectados, escenas afectadas, etc
      *
      */
     public void showEventItems(JList available, JList affected, Entrada selectedIn, PpalView ppalView) {
         int prefixBalast = Integer.parseInt(PropHandler.getProperty("in.out.type.balast"));
         int prefixGroup = Integer.parseInt(PropHandler.getProperty("in.out.type.group"));
         int prefixScene = Integer.parseInt(PropHandler.getProperty("in.out.type.scene"));
         HashMap<String, Balasto> balasts = ppalView.getBalasts();
 
         if (ppalView.getInOutType() == prefixBalast) {  //Balastos
             //Afected balasts
             new BalastosControl().readElements(ppalView);
             DefaultListModel inAffecBalasts = new DefaultListModel();
             int[] selectedBalasts = selectedIn.getBalastos();
             ArrayList sel = new ArrayList();
             for (int i = 0; i < selectedBalasts.length; i++) {
                 if (selectedBalasts[i] == 1) {
                     Balasto sce = balasts.get(String.valueOf(i));
                     if (sce != null) {
                         inAffecBalasts.addElement(sce.getBalastNumber() + " - " + sce.getName());
                         sel.add(String.valueOf(i));
                     }
                 }
             }
             affected.setModel(inAffecBalasts);
 
             //Available balasts
             DefaultListModel modelo = new DefaultListModel();
             ArrayList<String> addedBalasts = PropHandler.getAddedBalasts(ppalView.getDao());
             for (String balastNumber : addedBalasts) {
                 if (!sel.contains(balastNumber)) {
                     Balasto balasto = balasts.get(balastNumber);
                     modelo.addElement(balasto.getBalastNumber() + " - " + balasto.getName());
                 }
             }
             available.setModel(modelo);
         } else if (ppalView.getInOutType() == prefixGroup) { //Grupos
             //Afected balasts
             new GrupoControl().readElements(ppalView);
             DefaultListModel groupBalasts = new DefaultListModel();
             int[] selectedGroups = selectedIn.getGrupos();
             ArrayList sel = new ArrayList();
             for (int i = 0; i < selectedGroups.length; i++) {
                 if (selectedGroups[i] == 1) {
                     Grupo sce = ppalView.getGroups().get(String.valueOf(i));
                     if (sce != null) {
                         groupBalasts.addElement(sce.getGroupNumber() + " - " + sce.getName());
                         sel.add(String.valueOf(i));
                     }
                 }
             }
             affected.setModel(groupBalasts);
 
             //Available balasts
             DefaultListModel modelo = new DefaultListModel();
             ArrayList<String> addedGroups = PropHandler.getAddedGroups(ppalView.getDao());
             for (String groupNumber : addedGroups) {
                 if (sel.size() > 0 && !sel.contains(groupNumber)) {
                     Grupo grupo = ppalView.getGroups().get(groupNumber);
                     modelo.addElement(grupo.getGroupNumber() + " - " + grupo.getName());
                 }
             }
             available.setModel(modelo);
         } else if (ppalView.getInOutType() == prefixScene) {    //Escenas
             //Afected balasts
             new EscenaControl().readElements(ppalView);
             DefaultListModel sceneBalastsL = new DefaultListModel();
             int[] selectedBalasts = selectedIn.getEscenas();
             ArrayList sel = new ArrayList();
             for (int i = 0; i < selectedBalasts.length; i++) {
                 if (selectedBalasts[i] == 1) {
                     Escena sce = ppalView.getScenes().get(String.valueOf(i));
                     if (sce != null) {
                         sceneBalastsL.addElement(sce.getNumeroEscena() + " - " + sce.getNombre());
                         sel.add(String.valueOf(i));
                     }
                 }
             }
             affected.setModel(sceneBalastsL);
 
             //Available balasts
             DefaultListModel modelo = new DefaultListModel();
             ArrayList<String> addedBalasts = PropHandler.getAddedScenes(ppalView.getDao());
             for (String balastNumber : addedBalasts) {
                 if (!sel.contains(balastNumber)) {
                     Balasto balasto = balasts.get(balastNumber);
                     modelo.addElement(balasto.getBalastNumber() + " - " + balasto.getName());
                 }
             }
             available.setModel(modelo);
         }
     }
 
     /**
      * Update scene balast value.
      */
     private void updateEventItemLevel(PpalView ppalView) {
         String level = ppalView.getjTextField23().getText();
         String[] selectedBalast = ppalView.getBalastosAfectados_jList().getSelectedValue().toString().split(": ");
         String selectedBalastIdx = ppalView.getjLabel19().getText();
         DefaultListModel model = new DefaultListModel();
 
         String newValue = selectedBalast[0] + ": " + level;
 
         //Insert the modified balast value in the list.
         if (!selectedBalastIdx.equals("#")) {
             ListModel selected = ppalView.getBalastosAfectados_jList().getModel();
             for (int i = 0; i < selected.getSize(); i++) {
 
                 if (selected.getElementAt(i).toString().split(" - ")[0].equals(selectedBalastIdx)) {
                     model.addElement(newValue);
                 } else {
                     model.addElement(selected.getElementAt(i).toString());
                 }
             }
             ppalView.getBalastosAfectados_jList().setModel(model);
         } else {
             Validacion.showAlertMessage("Seleccione un balasto");
         }
 
     }
 
     /**
      * Select the type of event.
      */
     public void selectByDays(PpalView ppalView) {
         if (ppalView.getPorDiasEvento_jCheckBox().isSelected()) {
             //por dias
             ppalView.getjLabel16().setEnabled(true);
             ppalView.getjLabel25().setEnabled(true);
             ppalView.getRepetirCicloEvento_jCheckBox().setEnabled(true);
             ppalView.getLunes_jCheckBox().setEnabled(true);
             ppalView.getMartes_jCheckBox().setEnabled(true);
             ppalView.getMiercoles_jCheckBox().setEnabled(true);
             ppalView.getJuevesjCheckBox().setEnabled(true);
             ppalView.getViernes_jCheckBox().setEnabled(true);
             ppalView.getSabado_jCheckBox().setEnabled(true);
             ppalView.getDomingo_jCheckBox().setEnabled(true);
 
             ppalView.getjFormattedTextField3().setEnabled(true);
 
             //por fecha
             ppalView.getjDateChooser2().setEnabled(false);
             ppalView.getjLabel15().setEnabled(false);
             ppalView.getjLabel24().setEnabled(false);
             ppalView.getjFormattedTextField2().setEnabled(false);
 
         } else {
             ppalView.getjLabel16().setEnabled(false);
             ppalView.getjLabel25().setEnabled(false);
             ppalView.getRepetirCicloEvento_jCheckBox().setEnabled(false);
             ppalView.getLunes_jCheckBox().setEnabled(false);
             ppalView.getMartes_jCheckBox().setEnabled(false);
             ppalView.getMiercoles_jCheckBox().setEnabled(false);
             ppalView.getJuevesjCheckBox().setEnabled(false);
             ppalView.getViernes_jCheckBox().setEnabled(false);
             ppalView.getSabado_jCheckBox().setEnabled(false);
             ppalView.getDomingo_jCheckBox().setEnabled(false);
 
             ppalView.getLunes_jCheckBox().setSelected(false);
             ppalView.getMartes_jCheckBox().setSelected(false);
             ppalView.getMiercoles_jCheckBox().setSelected(false);
             ppalView.getJuevesjCheckBox().setSelected(false);
             ppalView.getViernes_jCheckBox().setSelected(false);
             ppalView.getSabado_jCheckBox().setSelected(false);
             ppalView.getDomingo_jCheckBox().setSelected(false);
 
             ppalView.getjFormattedTextField3().setEnabled(false);
 
             //por fecha
             ppalView.getjDateChooser2().setEnabled(true);
             ppalView.getjLabel15().setEnabled(true);
             ppalView.getjLabel24().setEnabled(true);
             ppalView.getjFormattedTextField2().setEnabled(true);
         }
     }
 
     public void selectElements(PpalView ppalView) {
         String[] balasto = ppalView.getjList13().getSelectedValue().toString().split(": ");
         String balastNumber = ppalView.getjList13().getSelectedValue().toString().split(" - ")[0];
         if (balasto.length > 1) {
             ppalView.getjLabel23().setText(balastNumber);
             ppalView.getjTextField25().setText(balasto[balasto.length - 1]);
         } else {
             ppalView.getjLabel23().setText(balastNumber);
             ppalView.getjTextField25().setText("0");
         }
     }
 
     /**
      * Metodo que actualiza el nivel del evento.
      *
      * @param ppalView
      */
     public void updateLevel(PpalView ppalView) {
         String level = ppalView.getjTextField25().getText();
         String[] selectedBalast = ppalView.getjList13().getSelectedValue().toString().split(": ");
         String selectedBalastIdx = ppalView.getjLabel23().getText();
         DefaultListModel model = new DefaultListModel();
 
         String newValue = selectedBalast[0] + ": " + level;
 
         //Insert the modified balast value in the list.
         if (!selectedBalastIdx.equals("#")) {
             ListModel selected = ppalView.getjList13().getModel();
             for (int i = 0; i < selected.getSize(); i++) {
 
                 if (selected.getElementAt(i).toString().split(" - ")[0].equals(selectedBalastIdx)) {
                     model.addElement(newValue);
                 } else {
                     model.addElement(selected.getElementAt(i).toString());
                 }
             }
             ppalView.getjList13().setModel(model);
         } else {
             Validacion.showAlertMessage("Seleccione un balasto");
         }
     }
 
     /**
      *
      * @param by
      * @param modo
      * @param ppalView
      */
     private void selectAffectedDays(StringBuffer by, int modo, PpalView ppalView) {
 
         if (modo == Conversion.BIGENDIAN) {
             for (int i = 0; i < by.length(); i++) {
                 procesarDias(by, ppalView, i);
             }
 
         } else {
             int j = 0;
             for (int i = by.length() - 1; i >= 0; i--) {
                 if (by.charAt(j) == '1') {
                     procesarDias(by, ppalView, i);
                 }
                 j++;
             }
         }
     }
 
     private void procesarDias(StringBuffer by, PpalView ppalView, int actual) {
 
         switch (actual) {
             case 0://domingo
                 ppalView.getDomingo_jCheckBox().setSelected(true);
 
                 break;
             case 1://lunes
                 ppalView.getLunes_jCheckBox().setSelected(true);
 
                 break;
             case 2://martes
                 ppalView.getMartes_jCheckBox().setSelected(true);
 
                 break;
             case 3://miercoles
                 ppalView.getMiercoles_jCheckBox().setSelected(true);
 
                 break;
             case 4://Jueves
                 ppalView.getJuevesjCheckBox().setSelected(true);
 
                 break;
             case 5://Viernes
                 ppalView.getViernes_jCheckBox().setSelected(true);
 
                 break;
             case 6://sabado
                 ppalView.getSabado_jCheckBox().setSelected(true);
 
                 break;
             default:
                 ppalView.getDomingo_jCheckBox().setSelected(true);
                 ppalView.getLunes_jCheckBox().setSelected(true);
                 ppalView.getMartes_jCheckBox().setSelected(true);
                 ppalView.getMiercoles_jCheckBox().setSelected(true);
                 ppalView.getJuevesjCheckBox().setSelected(true);
                 ppalView.getViernes_jCheckBox().setSelected(true);
                 ppalView.getSabado_jCheckBox().setSelected(true);
         }
     }
 
 //    /**
 //     * Método que refresca la vista de los eventos.
 //     *
 //     * @param ppalView
 //     */
 //    public void refrescarVista(PpalView ppalView) {
 //        cleanEventView(ppalView);
 ////        showAvailableEvents(ppalView);
 //        filterAddedEvent(ppalView);
 //    }
 
     @Override
     public void refrescarVista(PpalView ppalView) {
         cleanView(ppalView);
         showAvailableEvents(ppalView);
         filterAddedElements(ppalView);
 
         String[] elementosDisponibles = elementosDisponibles(ppalView);
         Validacion.actualizarCombo(ppalView.getEventoNum_jComboBox(), elementosDisponibles, Validacion.BALASTOS_DISPONIBLES);
     }
 
     @Override
     public String[] elementosDisponibles(PpalView ppalView) {
         EventoDAOJmodbus dao = new EventoDAOJmodbus(ppalView.getDao());
         String[] ses;
         ses = dao.elementosSinGrabar();
 
         return ses;
     }
 }
