 package heartdoctor.gui_controllers;
 
 import heartdoctor.DataModel.MedicalData;
 import heartdoctor.DataModel.PatientData;
 import heartdoctor.DataModel.PatientSearchResults;
 import heartdoctor.GUI.SearchPatients;
 import heartdoctor.Util.PatientController;
 import heartdoctor.ann.NeuralNetwork;
 import heartdoctor.application.AppController;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Klasa kontrolera do zarzadzania wyswietlaniem listy pacjentow w 
  * widoku SearchPatients.
  * @author michal
  * @author Qba
  */
 public class PatientResultsController {
 
     SearchPatients panel;
     PatientSearchResults model;
     int activeRecord;
 
     /**
      * Tworzy nowy kontroler
      * @param searchPanel Panel do kontrolowania
      * @param model Model do wyswietlania
      */
     public PatientResultsController(SearchPatients searchPanel, PatientSearchResults model) {
         panel = searchPanel;
         this.model = model;
 
     }
 
     /**
      * Inicjalizacja widoku, najpierw nalezy stworzyc kontroler, zainicjalizowac
      * komponenty widoku a pozniej uzyc funkcji initView
      */
     public void initView() {
         panel.patientListBox.removeAllItems();
         for (PatientData patient : model.getPatients()) {
             panel.patientListBox.addItem(patient.getName() + " " + patient.getLastName());
         }
         activeRecord = 0;
         panel.prevButton.setEnabled(false);
         panel.nextButton.setEnabled(model.getPatients().size() > 1);
         if (model.getPatients().size() > activeRecord) {
             panel.showPatientData(model.getPatient(activeRecord));
         } else {
             panel.clearPatientData();
         }
     }
 
     /**
      * Odswieza GUI po zmianie danych do wyswietlania
      */
     public void updateGUI() {
         panel.prevButton.setEnabled(activeRecord > 0);
         panel.nextButton.setEnabled(activeRecord < model.getPatients().size() - 1);
         panel.patientListBox.setSelectedIndex(activeRecord);
         panel.showPatientData(model.getPatient(activeRecord));
     }
 
     /**
      * Wyswietla nastepnego pacjenta na liscie
      */
     public void nextResult() {
         if (activeRecord >= model.getPatients().size() - 1) {
             return;
         }
         ++activeRecord;
         updateGUI();
     }
 
     /**
      * Wyswietla poprzedniego klienta na liscie
      */
     public void previousResult() {
         if (activeRecord < 1) {
             return;
         }
         --activeRecord;
         updateGUI();
     }
 
     /**
      * Wyswietla pacjenta o podanym indeksie
      * @param index 
      */
     public void selectResult(int index) {
         if (index >= 0 || index < model.getPatients().size()) {
             activeRecord = index;
             updateGUI();
         }
 
     }
 
     /**
      * Zwraca aktualnie wyswietlanego pacjenta
      * @return 
      */
     public PatientData getActivePatient() {
         return model.getPatient(activeRecord);
     }
 
     /**
      * obsluguje akcje zapisania pacjenta/danych z gui
      * @return true jeśli zapis się powiódł, false w przeciwnym wypadku
      */
     public boolean saveActivePatient(MedicalData medicalData) {
        PatientData patient=getActivePatient();
        int dataID=patient.getMedicalData().getDbID();
        patient.setMedicalData(medicalData);
        patient.getMedicalData().setDbID(dataID);
         try {
             return PatientController.updateMedicalRecord(getActivePatient().getMedicalData());
         } catch (SQLException ex) {
             Logger.getLogger(PatientResultsController.class.getName()).log(Level.SEVERE, null, ex);
             return false;
         }
     }
 
 
     /**
      * Wczytuje wszystkich pacjentow do formularza
      */
     public void loadAllPatients() {
         model.loadAllPatients();
         initView();
     }
 
     /**
      * Wczytuje nie zdiagnozowanych pacjentow do formularza
      */
     public void loadNotDiagnosedPatients() {
         model.loadNotDiagnosedPatients();
         initView();
     }
 
     /**
      * funkcja oblsugujaca klikniecie w search z widoku search patients
      * @param condition Warunek jaki musi spelnic pacjent
      * @param searchBy Pole po którym będą przeszukiwani pacjenci
      * @param value  Wartosc poszukiwanego parametru
      * @return Lista pacjentow zgodnych z warunkiem wyszukiwania
      */
     public ArrayList<PatientData> prepareResults(String searchBy, String condition, String value) {
         model.search(PatientSearchResults.generateSQL(searchBy, condition, value));
         initView();
         return model.getPatients();
     }
 
     /**
      * Obluga kilkniecia w diagnose z gui
      * 1. bierzemy sobie siec neuronowa
      * 2. wpychamy do niej dane aktywnego pacjenta
      * 3. dostajemy wynik: chory lub nie
      */
     public void diagnose() {
         getActivePatient();
         try {
             NeuralNetwork neuralNetwork=AppController.getNeuralNetwork();
             ArrayList<Double> outputs;
             outputs = neuralNetwork.feedForward(
                     convertMedicalData(getActivePatient().getMedicalData()));
             getActivePatient().getMedicalData().setProgramDiagnosis(outputs.get(0)>0.7? 1:0);
         } catch (Exception ex) {
             Logger.getLogger(PatientResultsController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Konwertuje medical data na liste dobule dla neural network
      * @param medicalData Dane medyczne do konwersji
      * @return Skonwertowane dane medyczne zgodne z formatem obsługiwanym przez ANN
      */
     private ArrayList<Double> convertMedicalData(MedicalData medicalData) {
         ArrayList<Double> list = new ArrayList<Double>();
         list.add(medicalData.getAge());
         list.add(medicalData.getSex());
         list.add(medicalData.getChestPain());
         list.add(medicalData.getBloodPressure());
         list.add(medicalData.getCholestoral());
         list.add(medicalData.getBloodSugar());
         list.add(medicalData.getRestecg());
         list.add(medicalData.getMaxHeartRate());
         list.add(medicalData.getAngina());
         list.add(medicalData.getOldpeak());
         list.add(medicalData.getSlope());
         list.add(medicalData.getCa());
         list.add(medicalData.getThal());
         return list;
     }
 }
