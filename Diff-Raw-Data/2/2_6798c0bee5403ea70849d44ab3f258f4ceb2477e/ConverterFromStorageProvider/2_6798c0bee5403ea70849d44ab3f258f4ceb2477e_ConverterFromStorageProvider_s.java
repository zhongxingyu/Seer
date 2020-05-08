 package net.ostis.confman.model.datastore.local.convert;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.ostis.confman.model.datastore.StorageProvider;
 import net.ostis.confman.model.entity.AcademicInformation;
 import net.ostis.confman.model.entity.Address;
 import net.ostis.confman.model.entity.Conference;
 import net.ostis.confman.model.entity.ContactInformation;
 import net.ostis.confman.model.entity.Participant;
 import net.ostis.confman.model.entity.ParticipantArrival;
 import net.ostis.confman.model.entity.ParticipantRole;
 import net.ostis.confman.model.entity.Person;
 import net.ostis.confman.model.entity.Report;
 import net.ostis.confman.model.entity.Section;
 import net.ostis.confman.model.entity.WorkplaceInformation;
 import net.ostis.confman.services.common.model.FullModel;
 
 public class ConverterFromStorageProvider {
 
     private static FullModel model;
 
     public ConverterFromStorageProvider() {
 
         super();
     }
 
     public FullModel convertData() {
 
         if (model != null) {
             return model;
         }
         model = new FullModel();
         final StorageProvider storageProvider = StorageProvider.getInstance();
         final List<Person> persons = storageProvider.getPersons();
         final Map<Long, net.ostis.confman.services.common.model.Person> personsMap = new HashMap<>();
         final List<Participant> participants = storageProvider
                 .getParticipants();
         final Map<Long, net.ostis.confman.services.common.model.Participant> participantsMap = new HashMap<>();
         final List<Report> reports = storageProvider.getReports();
         final Map<Long, net.ostis.confman.services.common.model.Report> reportsMap = new HashMap<>();
         final List<Section> sections = storageProvider.getSections();
         final Map<Long, net.ostis.confman.services.common.model.Section> sectionsMap = new HashMap<>();
         final List<Conference> conferences = storageProvider.getConferences();
         final Map<Long, net.ostis.confman.services.common.model.Conference> conferencesMap = new HashMap<>();
         model.setPersons(convertPersons(persons, personsMap));
         model.setParticipants(convertParticipants(participants, personsMap,
                 participantsMap));
         model.setReports(convertReports(reports, participantsMap, reportsMap));
         model.setSections(convertSections(sections, sectionsMap, reportsMap));
         model.setConferences(convereConferences(conferences, conferencesMap,
                 participantsMap, reportsMap, sectionsMap));
         setExtraParticipantInfo(participantsMap, conferencesMap, reportsMap,
                 participants);
         setExtraReportInfo(sectionsMap, reportsMap, reports);
         setExtraSectionsInfo(conferencesMap, sectionsMap, sections);
         return model;
     }
 
     private List<net.ostis.confman.services.common.model.Person> convertPersons(
             final List<Person> persons,
             final Map<Long, net.ostis.confman.services.common.model.Person> personsMap) {
 
         final List<net.ostis.confman.services.common.model.Person> listPersons = new ArrayList<>();
         for (final Person temp : persons) {
             final net.ostis.confman.services.common.model.Person person = new net.ostis.confman.services.common.model.Person();
             person.setFirstName(temp.getFirstName());
             person.setPatronymic(temp.getPatronymic());
             person.setSurname(temp.getSurname());
             person.setContacts(converContactInfo(temp.getContacts()));
             person.setWorkplace(convertWorplaceInfo(temp.getWorkplace()));
             person.setDegree(convertDegree(temp.getDegree()));
             person.setResidence(convertResidence(temp.getResidence()));
             personsMap.put(temp.getId(), person);
             listPersons.add(person);
         }
         return listPersons;
     }
 
     private net.ostis.confman.services.common.model.ContactInformation converContactInfo(
             final ContactInformation contactInformation) {
 
         final net.ostis.confman.services.common.model.ContactInformation information = new net.ostis.confman.services.common.model.ContactInformation();
         if (contactInformation == null) {
             return information;
         }
         information.setContactPhoneNumber(contactInformation.getPhone());
         information.seteMail(contactInformation.getEmail());
         return information;
     }
 
     private net.ostis.confman.services.common.model.WorkplaceInformation convertWorplaceInfo(
             final WorkplaceInformation workplaceInformation) {
 
         final net.ostis.confman.services.common.model.WorkplaceInformation information = new net.ostis.confman.services.common.model.WorkplaceInformation();
         if (workplaceInformation == null) {
             return information;
         }
         information.setAffliation(workplaceInformation.getWorkplace());
         information.setPosition(workplaceInformation.getPosition());
         return information;
     }
 
     private net.ostis.confman.services.common.model.AcademicInformation convertDegree(
             final AcademicInformation academicInformation) {
 
         final net.ostis.confman.services.common.model.AcademicInformation information = new net.ostis.confman.services.common.model.AcademicInformation();
         if (academicInformation == null) {
             return information;
         }
         information.setDegree(academicInformation.getDegree());
         information.setTitle(academicInformation.getTitle());
         return information;
     }
 
     private net.ostis.confman.services.common.model.Address convertResidence(
             final Address address) {
 
         final net.ostis.confman.services.common.model.Address personAddress = new net.ostis.confman.services.common.model.Address();
         if (address == null) {
             return personAddress;
         }
         personAddress.setCity(address.getCity());
         personAddress.setCountry(address.getCountry());
         personAddress.setHouseNumber(address.getHouseNumber());
         personAddress.setStreet(address.getStreet());
         return personAddress;
     }
 
     private List<net.ostis.confman.services.common.model.Participant> convertParticipants(
             final List<Participant> participants,
             final Map<Long, net.ostis.confman.services.common.model.Person> personsMap,
             final Map<Long, net.ostis.confman.services.common.model.Participant> participantsMap) {
 
         final List<net.ostis.confman.services.common.model.Participant> listParticipants = new ArrayList<>();
         for (final Participant temp : participants) {
             final net.ostis.confman.services.common.model.Participant participant = new net.ostis.confman.services.common.model.Participant();
            participant.setPerson(personsMap.get(temp.getId()));
             participant
                     .setArrival(convertParticipantArrival(temp.getArrival()));
             participant.setRole(convertParticipantRole(temp.getRole()));
             participantsMap.put(temp.getId(), participant);
             listParticipants.add(participant);
         }
         return listParticipants;
     }
 
     private void setExtraParticipantInfo(
             final Map<Long, net.ostis.confman.services.common.model.Participant> participantsMap,
             final Map<Long, net.ostis.confman.services.common.model.Conference> conferencesMap,
             final Map<Long, net.ostis.confman.services.common.model.Report> reportsMap,
             final List<Participant> listParticipants) {
 
         for (final Participant temp : listParticipants) {
             final net.ostis.confman.services.common.model.Participant participant = participantsMap
                     .get(temp.getId());
             participant
                     .setConference(conferencesMap.get(temp.getConferenceId()));
             final List<Long> reportsId = temp.getReportId();
             final List<net.ostis.confman.services.common.model.Report> reports = new ArrayList<>();
             for (final Long tempId : reportsId) {
                 reports.add(reportsMap.get(tempId));
             }
             participant.setReports(reports);
         }
     }
 
     private net.ostis.confman.services.common.model.ParticipantArrival convertParticipantArrival(
             final ParticipantArrival participantArrival) {
 
         final net.ostis.confman.services.common.model.ParticipantArrival arrival = new net.ostis.confman.services.common.model.ParticipantArrival();
         if (participantArrival == null) {
             return arrival;
         }
         arrival.setHousing(participantArrival.getHousing());
         arrival.setMeeting(participantArrival.getMeeting());
         arrival.setResidencePlace(convertResidence(participantArrival
                 .getResidencePlace()));
         return arrival;
     }
 
     private net.ostis.confman.services.common.model.ParticipantRole convertParticipantRole(
             final ParticipantRole participantRole) {
 
         final net.ostis.confman.services.common.model.ParticipantRole role = new net.ostis.confman.services.common.model.ParticipantRole();
         if (participantRole == null) {
             return role;
         }
         role.setExibitionStand(participantRole.getExhibitionStand());
         role.setParticipationForm(participantRole.getParticipationForm());
         role.setProgramCommitteeMember(participantRole
                 .getProgramCommitteeMember());
         return role;
     }
 
     private List<net.ostis.confman.services.common.model.Report> convertReports(
             final List<Report> reports,
             final Map<Long, net.ostis.confman.services.common.model.Participant> participantMap,
             final Map<Long, net.ostis.confman.services.common.model.Report> reportsMap) {
 
         final List<net.ostis.confman.services.common.model.Report> listReports = new ArrayList<>();
         for (final Report temp : reports) {
             final net.ostis.confman.services.common.model.Report report = new net.ostis.confman.services.common.model.Report();
             report.setTitle(temp.getTitle());
             report.setMainAuthor(participantMap.get(temp.getReporter()));
             report.setAllAuthors(findNecessaryParticipants(participantMap,
                     temp.getParticipants()));
             reportsMap.put(temp.getId(), report);
             listReports.add(report);
         }
         return listReports;
     }
 
     private void setExtraReportInfo(
             final Map<Long, net.ostis.confman.services.common.model.Section> sectionsMap,
             final Map<Long, net.ostis.confman.services.common.model.Report> reportsMap,
             final List<Report> reports) {
 
         for (final Report temp : reports) {
             final net.ostis.confman.services.common.model.Report report = reportsMap
                     .get(temp.getId());
             report.setSection(sectionsMap.get(temp.getSectionId()));
         }
     }
 
     private List<net.ostis.confman.services.common.model.Participant> findNecessaryParticipants(
             final Map<Long, net.ostis.confman.services.common.model.Participant> participantsMap,
             final List<Long> participantId) {
 
         final List<net.ostis.confman.services.common.model.Participant> participantList = new ArrayList<>();
         for (final Long temp : participantId) {
             participantList.add(participantsMap.get(temp));
         }
         return participantList;
     }
 
     private List<net.ostis.confman.services.common.model.Section> convertSections(
             final List<Section> sections,
             final Map<Long, net.ostis.confman.services.common.model.Section> sectionsMap,
             final Map<Long, net.ostis.confman.services.common.model.Report> reportsMap) {
 
         final List<net.ostis.confman.services.common.model.Section> sectionsList = new ArrayList<>();
         for (final Section temp : sections) {
             final net.ostis.confman.services.common.model.Section section = new net.ostis.confman.services.common.model.Section();
             section.setTitle(temp.getTitle());
             section.setDate(temp.getDate());
             section.setReports(findNecessaryReports(reportsMap,
                     temp.getReports()));
             sectionsMap.put(temp.getId(), section);
             sectionsList.add(section);
         }
         return sectionsList;
     }
 
     private void setExtraSectionsInfo(
             final Map<Long, net.ostis.confman.services.common.model.Conference> conferencesMap,
             final Map<Long, net.ostis.confman.services.common.model.Section> sectionsMap,
             final List<Section> sections) {
 
         for (final Section temp : sections) {
             final net.ostis.confman.services.common.model.Section section = sectionsMap
                     .get(temp.getId());
             section.setConference(conferencesMap.get(temp.getConferenceId()));
         }
     }
 
     private List<net.ostis.confman.services.common.model.Report> findNecessaryReports(
             final Map<Long, net.ostis.confman.services.common.model.Report> reportsMap,
             final List<Long> reportsId) {
 
         final List<net.ostis.confman.services.common.model.Report> reportsList = new ArrayList<>();
         for (final Long temp : reportsId) {
             reportsList.add(reportsMap.get(temp));
         }
         return reportsList;
     }
 
     private List<net.ostis.confman.services.common.model.Conference> convereConferences(
             final List<Conference> conferences,
             final Map<Long, net.ostis.confman.services.common.model.Conference> conferencesMap,
             final Map<Long, net.ostis.confman.services.common.model.Participant> participantsMap,
             final Map<Long, net.ostis.confman.services.common.model.Report> reportsMap,
             final Map<Long, net.ostis.confman.services.common.model.Section> sectionsMap) {
 
         final List<net.ostis.confman.services.common.model.Conference> listConferences = new ArrayList<>();
         for (final Conference temp : conferences) {
             final net.ostis.confman.services.common.model.Conference conference = new net.ostis.confman.services.common.model.Conference();
             conference.setTitle(temp.getTitle());
             conference.setStartDate(temp.getStartDate());
             conference.setEndDate(temp.getEndDate());
             conference.setConferenceVenue(convertConferenceVenue(temp
                     .getResidence()));
             conference.setParticipants(setConfParticipants(participantsMap,
                     temp.getParticipants()));
             conference
                     .setReports(setConfReports(reportsMap, temp.getReports()));
             conference.setSections(setConfSections(sectionsMap,
                     temp.getSections()));
             conferencesMap.put(temp.getId(), conference);
             listConferences.add(conference);
         }
         return listConferences;
     }
 
     private net.ostis.confman.services.common.model.Address convertConferenceVenue(
             final Address address) {
 
         final net.ostis.confman.services.common.model.Address confAddress = new net.ostis.confman.services.common.model.Address();
         if (address == null) {
             return confAddress;
         }
         confAddress.setCity(address.getCity());
         confAddress.setCountry(address.getCountry());
         confAddress.setHouseNumber(address.getHouseNumber());
         confAddress.setStreet(address.getStreet());
         return confAddress;
     }
 
     private List<net.ostis.confman.services.common.model.Participant> setConfParticipants(
             final Map<Long, net.ostis.confman.services.common.model.Participant> participantsMap,
             final List<Long> participantsId) {
 
         final List<net.ostis.confman.services.common.model.Participant> participantsList = new ArrayList<>();
         for (final Long temp : participantsId) {
             participantsList.add(participantsMap.get(temp));
         }
         return participantsList;
     }
 
     private List<net.ostis.confman.services.common.model.Report> setConfReports(
             final Map<Long, net.ostis.confman.services.common.model.Report> reportsMap,
             final List<Long> reportsId) {
 
         final List<net.ostis.confman.services.common.model.Report> reportsList = new ArrayList<>();
         for (final Long temp : reportsId) {
             reportsList.add(reportsMap.get(temp));
         }
         return reportsList;
     }
 
     private List<net.ostis.confman.services.common.model.Section> setConfSections(
             final Map<Long, net.ostis.confman.services.common.model.Section> sectionsMap,
             final List<Long> sectionsId) {
 
         final List<net.ostis.confman.services.common.model.Section> sectionsList = new ArrayList<>();
         for (final Long temp : sectionsId) {
             sectionsList.add(sectionsMap.get(temp));
         }
         return sectionsList;
     }
 }
