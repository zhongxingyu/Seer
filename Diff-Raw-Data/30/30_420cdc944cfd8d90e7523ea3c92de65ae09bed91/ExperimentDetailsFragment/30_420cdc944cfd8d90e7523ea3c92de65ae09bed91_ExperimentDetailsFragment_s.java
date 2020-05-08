 package cz.zcu.kiv.eeg.mobile.base.ui.experiment;
 
 import android.annotation.SuppressLint;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import cz.zcu.kiv.eeg.mobile.base.R;
 import cz.zcu.kiv.eeg.mobile.base.data.container.xml.*;
 
 /**
  * Fragment used for displaying details about chosen experiment.
  *
  * @author Petr Miko
  */
 @SuppressLint("SimpleDateFormat")
 public class ExperimentDetailsFragment extends Fragment {
 
     public final static String TAG = ExperimentDetailsFragment.class.getSimpleName();
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         //check if data are available and selection index is positive
         boolean hasData = getArguments().getParcelable("data") != null && getArguments().getInt("index", -1) >= 0;
 
         if (hasData) {
             View view = inflater.inflate(R.layout.base_experiment_details, container, false);
             initData(view);
             return view;
         } else {
             return inflater.inflate(R.layout.details_empty, container, false);
         }
     }
 
     /**
      * Method for filling element views with proper data.
      *
      * @param view object of layout, which contains element child views
      */
     private void initData(View view) {
         //get all view elements
         TextView experimentIdView = (TextView) view.findViewById(R.id.experimentIdValue);
         TextView scenarioNameView = (TextView) view.findViewById(R.id.scenarioNameValue);
         TextView fromTime = (TextView) view.findViewById(R.id.fromValue);
         TextView toTime = (TextView) view.findViewById(R.id.toValue);
         TextView envNote = (TextView) view.findViewById(R.id.experiment_env_note);
         TextView subjectName = (TextView) view.findViewById(R.id.experiment_subject);
         TextView subjectAge = (TextView) view.findViewById(R.id.experiment_subject_age);
         TextView subjectLeftHand = (TextView) view.findViewById(R.id.experiment_subject_lefthand);
         TextView subjectGender = (TextView) view.findViewById(R.id.experiment_subject_gender);
         TextView weatherTitle = (TextView) view.findViewById(R.id.experiment_weather_title);
         TextView weatherDescription = (TextView) view.findViewById(R.id.experiment_weather_description);
         TextView artifactCompensation = (TextView) view.findViewById(R.id.experiment_artifact_compensation);
         TextView artifactRejectCond = (TextView) view.findViewById(R.id.experiment_artifact_reject);
         TextView digitizationFilter = (TextView) view.findViewById(R.id.experiment_digitization_filter);
         TextView digitizationGain = (TextView) view.findViewById(R.id.experiment_digitization_gain);
         TextView digitizationRate = (TextView) view.findViewById(R.id.experiment_digitization_sampling_rate);
         TextView temperature = (TextView) view.findViewById(R.id.experiment_temperature);
         LinearLayout diseasesList = (LinearLayout) view.findViewById(R.id.experiment_diseases);
         LinearLayout hardwareList = (LinearLayout) view.findViewById(R.id.experiment_hardware_list);
         LinearLayout softwareList = (LinearLayout) view.findViewById(R.id.experiment_softwares);
         LinearLayout pharmaceuticals = (LinearLayout) view.findViewById(R.id.experiment_pharmaceuticals);
         TextView electrodeImpendace = (TextView) view.findViewById(R.id.experiment_electrode_impedance);
         TextView electrodeSystemDescription = (TextView) view.findViewById(R.id.experiment_electrode_system_description);
         TextView electrodeSystemName = (TextView) view.findViewById(R.id.experiment_electrode_system_title);
         LinearLayout electrodeLocations = (LinearLayout) view.findViewById(R.id.experiment_electrodes);
 
         Experiment experiment = (Experiment) getArguments().getParcelable("data");
 
         //setting data
         if (experiment != null) {
             experimentIdView.setText(Integer.toString(experiment.getExperimentId()));
             fromTime.setText(experiment.getStartTime().toString());
             toTime.setText(experiment.getEndTime().toString());
             scenarioNameView.setText(experiment.getScenario().getScenarioName());
             envNote.setText(experiment.getEnvironmentNote());
 
             Subject subject = experiment.getSubject();
             subjectName.setText(subject.getName() + " " + subject.getSurname());
             subjectGender.setText(subject.getGender());
             subjectAge.setText(Integer.toString(subject.getAge()));
             subjectLeftHand.setText(getString(subject.isLeftHanded() ? R.string.yes : R.string.no));
 
             Weather weather = experiment.getWeather();
             weatherTitle.setText(weather.getTitle());
             weatherDescription.setText(weather.getDescription());
 
             Artifact artifact = experiment.getArtifact();
             artifactCompensation.setText(artifact.getCompensation());
             artifactRejectCond.setText(artifact.getRejectCondition());
 
             Digitization digitization = experiment.getDigitization();
             digitizationFilter.setText(digitization.getFilter());
             digitizationGain.setText(Float.toString(digitization.getGain()));
             digitizationRate.setText(Float.toString(digitization.getSamplingRate()));
 
             if (experiment.getTemperature() != null) {
                 temperature.setText(experiment.getTemperature().toString());
             }
 
             ExperimentDetailLists.fillDiseaseList(diseasesList, experiment.getDiseases().isAvailable() ? experiment.getDiseases().getDiseases() : null);
             ExperimentDetailLists.fillHardwareList(hardwareList, experiment.getHardwareList().isAvailable() ? experiment.getHardwareList().getHardwareList() : null);
             ExperimentDetailLists.fillSoftwareList(softwareList, experiment.getSoftwareList().isAvailable() ? experiment.getSoftwareList().getSoftwareList() : null);
             ExperimentDetailLists.fillPharmaceuticals(pharmaceuticals, experiment.getPharmaceuticals().isAvailable() ? experiment.getPharmaceuticals().getPharmaceuticals() : null);
             fillElectrodes(electrodeImpendace, electrodeSystemName, electrodeSystemDescription, electrodeLocations, experiment);
         }
     }
 
     /**
      * Method for displaying electrode information.
      *
      * @param electrodeImpedance         set impedance
      * @param electrodeSystemName        name of electrode system
      * @param electrodeSystemDescription electrode system description
      * @param electrodeLocations         frame, where will be all electrode locations data inflated in
      * @param experiment                 original source object
      */
     private void fillElectrodes(TextView electrodeImpedance, TextView electrodeSystemName, TextView electrodeSystemDescription, LinearLayout electrodeLocations, Experiment experiment) {
 
         ElectrodeConf electrodeConf = experiment.getElectrodeConf();
         electrodeImpedance.setText(Integer.toString(electrodeConf.getImpedance()));
         ElectrodeSystem electrodeSystem = electrodeConf.getElectrodeSystem();
         electrodeSystemDescription.setText(electrodeSystem.getDescription());
         electrodeSystemName.setText(electrodeSystem.getTitle());
 
        ExperimentDetailLists.fillElectrodeLocations(electrodeLocations, electrodeConf.getElectrodeLocations().isAvailable() ? electrodeConf.getElectrodeLocations().getElectrodeLocations() : null);
     }
 }
