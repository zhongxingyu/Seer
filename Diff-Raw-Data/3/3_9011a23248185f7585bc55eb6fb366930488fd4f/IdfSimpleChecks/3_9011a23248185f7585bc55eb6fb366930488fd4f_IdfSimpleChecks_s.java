 /*
  * Copyright 2012 EMBL - European Bioinformatics Institute
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or impl
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.ebi.fg.annotare2.magetabcheck.checks.idf;
 
 import org.hamcrest.Matchers;
 import uk.ac.ebi.fg.annotare2.magetabcheck.checker.CheckApplicationType;
 import uk.ac.ebi.fg.annotare2.magetabcheck.checker.CheckPosition;
 import uk.ac.ebi.fg.annotare2.magetabcheck.checker.annotation.MageTabCheck;
 import uk.ac.ebi.fg.annotare2.magetabcheck.model.Cell;
 import uk.ac.ebi.fg.annotare2.magetabcheck.model.FileLocation;
 import uk.ac.ebi.fg.annotare2.magetabcheck.model.idf.*;
 
 import static com.google.common.base.Strings.isNullOrEmpty;
 import static java.lang.Boolean.FALSE;
 import static java.util.Arrays.asList;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checker.CheckApplicationType.MICRO_ARRAY_ONLY;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checker.CheckModality.WARNING;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checker.CheckPosition.createPosition;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checker.CheckPositionSetter.setCheckPosition;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checks.idf.IdfConstants.DATE_FORMAT;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checks.idf.IdfConstants.SUBMITTER_ROLE;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checks.matchers.IsDateString.isDateString;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checks.matchers.IsValidFileLocation.isValidFileLocation;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checks.matchers.RegExpMatcher.matches;
 
 /**
  * @author Olga Melnichuk
  */
 public class IdfSimpleChecks {
 
     @MageTabCheck(
             ref = "G01",
             value = "Experiment title must be specified")
     public void investigationTitleRequired(final Info info) {
         assertNotEmptyString(info.getTitle());
     }
 
     @MageTabCheck(
             ref = "G02",
             value = "Experiment description must be specified")
     public void experimentDescriptionRequired(Info info) {
         assertNotEmptyString(info.getExperimentDescription());
     }
 
     @MageTabCheck(
             ref = "G09",
             value = "Experiment description should be at least 50 characters long",
             modality = WARNING)
     public void experimentDescriptionShouldBe50CharsLong(Info info) {
         Cell<String> cell = info.getExperimentDescription();
         if (isNullOrEmpty(cell.getValue())) {
             return;
         }
         setPosition(cell);
         assertThat(cell.getValue().length(), greaterThan(50));
     }
 
     @MageTabCheck(
             ref = "G03",
             value = "Date of Experiment should be specified",
             modality = WARNING)
     public void dateOfExperimentShouldBeSpecified(Info info) {
         assertNotEmptyString(info.getDateOfExperiment());
     }
 
     @MageTabCheck(
             ref = "G04",
             value = "Date of Experiment must be in 'YYYY-MM-DD' format")
     public void dateOfExperimentFormat(Info info) {
         Cell<String> cell = info.getDateOfExperiment();
         if (isNullOrEmpty(cell.getValue())) {
             return;
         }
         setPosition(cell);
         assertThat(cell.getValue(), isDateString(DATE_FORMAT));
     }
 
     @MageTabCheck(
             ref = "G05",
             value = "Experiment public release date must be specified")
     public void publicReleaseDateRequired(Info info) {
         assertNotEmptyString(info.getPublicReleaseDate());
     }
 
     @MageTabCheck(
             ref = "G06",
             value = "Experiment public release date must be in 'YYYY-MM-DD' format")
     public void publicReleaseDateFormat(Info info) {
         Cell<String> cell = info.getPublicReleaseDate();
         if (isNullOrEmpty(cell.getValue())) {
             return;
         }
         setPosition(cell);
         assertThat(cell.getValue(), isDateString(DATE_FORMAT));
     }
 
     @MageTabCheck(
             ref = "G07",
             value = "Reference to the SDRF file must be specified")
     public void sdrfFileMustBeSpecified(Info info) {
         Cell<FileLocation> cell = info.getSdrfFile();
         setPosition(cell);
         assertThat(cell.getValue(), notNullValue());
         assertThat(cell.getValue().isEmpty(), is(FALSE));
     }
 
     @MageTabCheck(
             ref = "G08",
             value = "Reference to the SDRF file must be valid file location")
     public void sdrfFileMustBeValidLocation(Info info) {
         Cell<FileLocation> cell = info.getSdrfFile();
         FileLocation loc = cell.getValue();
         if (loc == null || loc.isEmpty()) {
             return;
         }
         setPosition(cell);
         assertThat(loc, isValidFileLocation());
     }
 
     @MageTabCheck(
             ref = "C02",
             value = "A contact must have last name specified")
     public void contactMustHaveLastName(Person person) {
         assertNotEmptyString(person.getLastName());
     }
 
     @MageTabCheck(
             ref = "C08",
             value = "A contact should have first name specified",
             modality = WARNING)
     public void contactShouldHaveFirstName(Person person) {
         assertNotEmptyString(person.getFirstName());
     }
 
     @MageTabCheck(
             ref = "C09",
             value = "A contact should have an affiliation specified",
             modality = WARNING)
     public void contactShouldHaveAffiliation(Person person) {
         assertNotEmptyString(person.getAffiliation());
     }
 
     @MageTabCheck(
             ref = "C10",
             value = "A contact role(s) should have a term source specified",
             modality = WARNING)
     public void contactRolesShouldHaveTermSource(Person person) {
         TermList roles = person.getRoles();
         if (roles == null || roles.isEmpty()) {
             return;
         }
         assertNotNull(roles.getSource());
     }
 
     @MageTabCheck(
             ref = "C06",
             value = "A contact with '" + SUBMITTER_ROLE + "' role must have affiliation specified",
             application = CheckApplicationType.HTS_ONLY)
     public void submitterMustHaveAffiliation(Person person) {
         TermList roles = person.getRoles();
         if (roles == null || roles.isEmpty()) {
             return;
         }
         if (roles.getNames().getValue().contains(SUBMITTER_ROLE)) {
             assertNotEmptyString(person.getAffiliation());
         }
     }
 
     @MageTabCheck(
             ref = "ED02",
             value = "An experimental design should be defined by a term",
             modality = WARNING,
             application = MICRO_ARRAY_ONLY)
     public void experimentDesignShouldHaveName(ExperimentalDesign exd) {
         assertNotEmptyString(exd.getName());
     }
 
     @MageTabCheck(
             ref = "ED03",
             value = "An experimental design term should have a term source",
             modality = WARNING,
             application = MICRO_ARRAY_ONLY)
     public void experimentalDesignShouldHaveTermSource(ExperimentalDesign exd) {
         assertNotNull(exd.getSource());
     }
 
     @MageTabCheck(
             ref = "EF02",
             value = "An experimental factor must have name specified")
     public void experimentalFactorMustHaveName(ExperimentalFactor exf) {
         assertNotEmptyString(exf.getName());
     }
 
     @MageTabCheck(
             ref = "EF03",
             value = "An experimental factor should have a type specified",
             modality = WARNING
     )
     public void experimentalFactorShouldHaveType(ExperimentalFactor exf) {
         assertNotEmptyString(exf.getType().getName());
     }
 
     @MageTabCheck(
             ref = "EF04",
             value = "An experimental factor type should have term source specified",
             modality = WARNING)
     public void experimentalFactorTypeShouldHaveSource(ExperimentalFactor exf) {
         assertNotNull(exf.getType().getSource());
     }
 
     @MageTabCheck(
             ref = "QC02",
             value = "A quality control type should be defined by a term",
             modality = WARNING)
     public void qualityControlTypeShouldHaveName(QualityControlType qt) {
         assertNotEmptyString(qt.getName());
     }
 
     @MageTabCheck(
             ref = "QC03",
             value = "A quality control type should have term source specified",
             modality = WARNING)
     public void qualityControlTypeShouldHaveSource(QualityControlType qt) {
         assertNotNull(qt.getSource());
     }
 
     @MageTabCheck(
             ref = "RT02",
             value = "A replicate type should be defined by a term",
             modality = WARNING)
     public void replicateTypeShouldHaveName(ReplicateType rt) {
         assertNotEmptyString(rt.getName());
     }
 
     @MageTabCheck(
             ref = "RT03",
             value = "A replicate type should have term source specified",
             modality = WARNING)
     public void replicateTypeShouldHaveSource(ReplicateType rt) {
         assertNotNull(rt.getSource());
     }
 
     @MageTabCheck(
             ref = "NT02",
             value = "A normalization type should be defined by a term",
             modality = WARNING)
     public void normalizationTypeShouldHaveName(NormalizationType nt) {
         assertNotEmptyString(nt.getName());
     }
 
     @MageTabCheck(
             ref = "NT03",
             value = "A normalization type should have term source specified",
             modality = WARNING)
     public void normalizationTypeShouldHaveSource(NormalizationType nt) {
         assertNotNull(nt.getSource());
     }
 
     @MageTabCheck(
             ref = "PB01",
             value = "A publication should have at least one of PubMed ID or Publication DOI specified",
             modality = WARNING)
     public void publicationShouldHavePubMedIDOrDOI(Publication pub) {
         setPosition(pub.getPubMedId());
         assertThat(asList(pub.getPubMedId().getValue(), pub.getPublicationDOI().getValue()),
                 Matchers.<String>hasItem(not(isEmptyOrNullString())));
     }
 
     @MageTabCheck(
             ref = "PB02",
             value = "PubMed Id must be numeric")
     public void pubMedIdMustBeNumeric(Publication pub) {
         Cell<String> cell = pub.getPubMedId();
         if (isNullOrEmpty(cell.getValue())) {
             return;
         }
         setPosition(cell);
         assertThat(pub.getPubMedId().getValue(), matches("[0-9]+"));
     }
 
     @MageTabCheck(
             ref = "PB03",
             value = "A publication authors should be specified",
             modality = WARNING)
     public void publicationShouldHaveAuthorsSpecified(Publication pub) {
         assertNotEmptyString(pub.getAuthorList());
     }
 
     @MageTabCheck(
             ref = "PB04",
             value = "A publication title should be specified",
             modality = WARNING)
     public void publicationShouldHaveTitleSpecified(Publication pub) {
         assertNotEmptyString(pub.getTitle());
     }
 
     @MageTabCheck(
             ref = "PB05",
             value = "A publication status should be specified",
             modality = WARNING)
     public void publicationShouldHaveStatusSpecified(Publication pub) {
         assertNotEmptyString(pub.getStatus().getName());
     }
 
     @MageTabCheck(
             ref = "PB06",
             value = "A publication status should have term source specified",
             modality = WARNING)
     public void publicationStatusShouldHaveTermSourceSpecified(Publication pub) {
         assertNotNull(pub.getStatus().getSource());
     }
 
     @MageTabCheck(
             ref = "PR02",
             value = "Name of a protocol must be specified")
     public void protocolMustHaveName(Protocol prot) {
         assertNotEmptyString(prot.getName());
     }
 
     @MageTabCheck(
             ref = "PR03",
             value = "A protocol type must be specified")
     public void protocolMustHaveType(Protocol prot) {
         assertNotEmptyString(prot.getType().getName());
     }
 
     @MageTabCheck(
             ref = "PR04",
             value = "A protocol type should have term source specified",
             modality = WARNING)
     public void protocolTypeShouldHaveSource(Protocol prot) {
         assertNotNull(prot.getType().getSource());
     }
 
     @MageTabCheck(
             ref = "PR05",
            value = "Description of a protocol should be specified",
            modality = WARNING)
     public void protocolShouldHaveDescription(Protocol prot) {
         assertNotEmptyString(prot.getDescription());
     }
 
     @MageTabCheck(
             ref = "PR06",
             value = "Description of a protocol should be over 50 characters long",
             modality = WARNING)
     public void protocolDescriptionShouldBeOver50CharsLong(Protocol prot) {
         Cell<String> cell = prot.getDescription();
         if (isNullOrEmpty(cell.getValue())) {
             return;
         }
         setPosition(cell);
         assertThat(cell.getValue().length(), greaterThan(50));
     }
 
     @MageTabCheck(
             ref = "PR07",
             value = "A protocol should have parameters",
             modality = WARNING)
     public void protocolShouldHaveParameters(Protocol prot) {
         setPosition(prot.getParameters());
         assertThat(prot.getParameters().getValue(), is(not(empty())));
     }
 
     @MageTabCheck(
             ref = "TS01",
             value = "Name of a term source must be specified")
     public void termSourceMustHaveName(TermSource ts) {
         assertNotEmptyString(ts.getName());
     }
 
     @MageTabCheck(
             ref = "TS03",
             value = "URL/File of a term source should be specified",
             modality = WARNING)
     public void termSourceShouldHaveFile(TermSource ts) {
         assertNotEmptyString(ts.getFile());
     }
 
     @MageTabCheck(
             ref = "TS04",
             value = "Version of a term source should be specified",
             modality = WARNING)
     public void termSourceShouldHaveVersion(TermSource ts) {
         assertNotEmptyString(ts.getVersion());
     }
 
     private static <T> void assertNotNull(Cell<T> cell) {
         setPosition(cell);
         assertThat(cell.getValue(), notNullValue());
     }
 
     private static void assertNotEmptyString(Cell<String> cell) {
         setPosition(cell);
         assertThat(cell.getValue(), notNullValue());
         assertThat(cell.getValue(), not(isEmptyString()));
     }
 
     private static void setPosition(Cell<?> cell) {
         setCheckPosition(createPosition(cell));
     }
 }
