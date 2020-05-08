 package org.openmrs.module.mirebalaisreports.definitions;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.Cohort;
 import org.openmrs.Encounter;
 import org.openmrs.EncounterType;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.Visit;
 import org.openmrs.VisitType;
 import org.openmrs.module.reporting.common.DateUtil;
 import org.openmrs.module.reporting.dataset.DataSet;
 import org.openmrs.module.reporting.dataset.DataSetRow;
 import org.openmrs.module.reporting.evaluation.EvaluationContext;
 import org.openmrs.module.reporting.report.ReportData;
 import org.openmrs.module.reporting.report.definition.ReportDefinition;
 import org.openmrs.module.reporting.report.renderer.TsvReportRenderer;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.Map;
 
 import static org.junit.Assert.assertThat;
 import static org.openmrs.module.reporting.common.ReportingMatchers.isCohortWithExactlyIds;
 import static org.openmrs.module.reporting.common.ReportingMatchers.isCohortWithExactlyMembers;
 
 public class DailyCheckInsReportManagerTest extends BaseMirebalaisReportTest {
 
     @Autowired
     DailyCheckInsReportManager manager;
 
     private Patient p2, p3, p4;
 
     @Before
     public void setUp() throws Exception {
         VisitType atFacility = emrApiProperties.getAtFacilityVisitType();
         Location registrationDesk = mirebalaisReportsProperties.getClinicRegistrationLocation();
         Location outpatient = mirebalaisReportsProperties.getOutpatientLocation();
         Location mirebalaisHospital = mirebalaisReportsProperties.getMirebalaisHospitalLocation();
         EncounterType registration = mirebalaisReportsProperties.getRegistrationEncounterType();
         EncounterType checkIn = mirebalaisReportsProperties.getCheckInEncounterType();
         EncounterType consult = mirebalaisReportsProperties.getConsultEncounterType();
 
         // never registered or seen
         data.randomPatient().dateCreated("2013-10-01").save();
 
         // registered at Clinic Registration, checked in at Outpatient Clinic for a CLINICAL visit (and had a consult)
         p2 = data.randomPatient().save();
         Visit v1 = data.visit().patient(p2).visitType(atFacility).started("2013-10-01 09:30:00").stopped("2013-10-01 16:45:00").location(mirebalaisHospital).save();
         data.encounter().visit(v1).encounterType(registration).location(registrationDesk).encounterDatetime("2013-10-01 09:30:00").save();
         Encounter p2CheckIn = data.encounter().visit(v1).encounterType(checkIn).location(outpatient).encounterDatetime("2013-10-01 09:45:00").save();
         data.obs().encounter(p2CheckIn).concept("Type of HUM visit", "PIH").value("CLINICAL", "PIH").save();
         data.encounter().visit(v1).encounterType(consult).encounterDatetime("2013-10-01 10:45:00").location(outpatient).save();
 
         // checked in at Outpatient Clinic for a Pharmacy only visit
         p3 = data.randomPatient().save();
         data.encounter().patient(p3).encounterType(registration).location(registrationDesk).encounterDatetime("2013-01-01 09:30:00").save();
         Visit v2 = data.visit().patient(p3).visitType(atFacility).started("2013-10-01 10:30:00").stopped("2013-10-01 16:45:00").location(mirebalaisHospital).save();
         Encounter p3checkIn = data.encounter().visit(v2).encounterType(checkIn).location(outpatient).encounterDatetime("2013-10-01 10:45:00").save();
         data.obs().encounter(p3checkIn).concept("Type of HUM visit", "PIH").value("Pharmacy only", "PIH").save();
 
         // registered before and had a consult, then checked in again today for a CLINICAL visit (but no consult yet)
         p4 = data.randomPatient().save();
         data.encounter().patient(p4).encounterType(consult).encounterDatetime("2009-01-01").location(outpatient).save();
         Visit v3 = data.visit().patient(p4).visitType(atFacility).started("2013-10-01 14:30:00").location(mirebalaisHospital).save();
         Encounter p4CheckIn = data.encounter().visit(v3).encounterType(checkIn).location(outpatient).encounterDatetime("2013-10-01 14:30:00").save();
         data.obs().encounter(p4CheckIn).concept("Type of HUM visit", "PIH").value("CLINICAL", "PIH").save();
     }
 
     @Test
     public void testReport() throws Exception {
         EvaluationContext context = new EvaluationContext();
         context.addParameterValue("day", DateUtil.parseDate("2013-10-01", "yyyy-MM-dd"));
 
         ReportDefinition reportDefinition = manager.constructReportDefinition();
         ReportData data = reportDefinitionService.evaluate(reportDefinition, context);
         new TsvReportRenderer().render(data, null, System.out);
 
         DataSet byLocation = data.getDataSets().get("byLocation");
         for (DataSetRow row : byLocation) {
             if (row.getColumnValue("rowLabel").equals("ui.i18n.Location.name.199e7d87-92a0-4398-a0f8-11d012178164")) {
                 for (Map.Entry<String, Object> e : row.getColumnValuesByKey().entrySet()) {
                    if ("PIH:CLINICAL-new".equals(e.getKey())) {
                         assertThat((Cohort) e.getValue(), isCohortWithExactlyMembers(p2));
                     }
                    else if ("PIH:CLINICAL-return".equals(e.getKey())) {
                         assertThat((Cohort) e.getValue(), isCohortWithExactlyMembers(p4));
                     }
                     else if ("Pharmacy only".equals(e.getKey())) {
                         assertThat((Cohort) e.getValue(), isCohortWithExactlyMembers(p3));
                     }
                     else if (!"rowLabel".equals(e.getKey())) {
                        if (((Cohort) e.getValue()).size() != 0) {
                            System.out.println("oops");
                        }
                         assertThat((Cohort) e.getValue(), isCohortWithExactlyIds());
                     }
                 }
             }
             else {
                 // everything else should be empty
                 for (Map.Entry<String, Object> e : row.getColumnValuesByKey().entrySet()) {
                     if (!"rowLabel".equals(e.getKey())) {
                         assertThat((Cohort) e.getValue(), isCohortWithExactlyIds());
                     }
                 }
             }
         }
     }
 
 }
