 package net.kevxu.purdueassist.course.elements;
 
 public class Predefined {
 
 	public enum Term {
 		CURRENT("Current", "CURRENT"), SUMMER2013("Summer 2013", "201330"), SPRING2013(
 				"Spring 2013", "201320"), FALL2012("Fall 2012", "201310"), SUMMER2012(
 				"Summer 2012", "201230"), SPRING2012("Spring 2012", "201220"), FALL2011(
 				"Fall 2011", "201210"), SUMMER2011("Summer 2011", "201130"), SPRING2011(
 				"Spring 2011", "201120"), FALL2010("Fall 2010", "201110"), SUMMER2010(
 				"Summer 2010", "201030"), SPRING2010("Spring 2010", "201020"), FALL2009(
 				"Fall 2009", "201010"), SUMMER2009("Summer 2009", "200930"), SPRING2009(
 				"Spring 2009", "200920"), FALL2008("Fall 2008", "200910"), SUMMER2008(
 				"Summer 2008", "200830"), SPRING2008("Spring 2008", "200820");
 
 		private final String name;
 		private final String linkName;
 
 		Term(String name, String linkName) {
 			this.name = name;
 			this.linkName = linkName;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public String getLinkName() {
 			return linkName;
 		}
 
 		@Override
 		public String toString() {
 			return name;
 		}
 	}
 
 	public enum Subject {
 		AAE("Aero & Astro Engineering"), AAS("African American Studies"), ABE(
 				"Agri & Biol Engineering"), AD("Art & Design"), AFT(
 				"Aerospace Studies"), AGEC("Agricultural Economics"), AGR(
 				"Agriculture"), AGRY("Agronomy"), AMST("American Studies"), ANSC(
 				"Animal Sciences"), ANTH("Anthropology"), ARAB("Arabic"), ASAM(
 				"Asian American Studies"), ASL("American Sign Language"), ASM(
 				"Agricultural Systems Mgmt"), ASTR("Astronomy"), AT(
 				"Aviation Technology"), BAND("Bands"), BCHM("Biochemistry"), BCM(
 				"Bldg Construct Mgmt Tech"), BIOL("Biological Sciences"), BME(
 				"Biomedical Engineering"), BMS("Basic Medical Sciences"), BTNY(
 				"Botany & Plant Pathology"), CAND("Candidate"), CE(
 				"Civil Engineering"), CEM("Construction Engr & Mgmt"), CGT(
 				"Computer Graphics Tech"), CHE("Chemical Engineering"), CHM(
 				"Chemistry"), CHNS("Chinese"), CLCS("Classics"), CLPH(
 				"Clinical Pharmacy"), CMPL("Comparative Literature"), CNIT(
 				"Computer & Info Tech"), COM("Communication"), CPB(
 				"Comparative Pathobiology"), CS("Computer Sciences"), CSR(
 				"Consumer ScI & Retailing"), DANC("Dance"), EAS(
 				"Earth & Atmospheric Sci"), ECE("Electrical & Computer Engr"), ECET(
 				"Electrical&Comp Engr Tech"), ECON("Economics"), EDCI(
 				"Educ-Curric & Instruction"), EDPS("Educ-Ed'l and Psy Studies"), EDST(
 				"Ed Leadrship&Cultrl Fnd"), EEE("Environ & Ecological Engr"), ENE(
 				"Engineering Education"), ENGL("English"), ENGR(
 				"First Year Engineering"), ENTM("Entomology"), ENTR(
 				"Entrepreneurship"), EPCS("Engr Proj Cmity Service"), FLL(
 				"Foreign Lang & Literatures"), FNR("Forestry&Natural Resources"), FR(
 				"French"), FS("Food Science"), FVS("Film And Video Studies"), GEP(
 				"Global Engineering Program"), GER("German"), GRAD(
 				"Graduate Studies"), GREK("Greek"), GS("General Studies"), HDFS(
 				"Human Dev &Family Studies"), HEBR("Hebrew"), HHS(
 				"College Health & Human Sci"), HIST("History"), HK(
 				"Health And Kinesiology"), HONR("Honors"), HORT("Horticulture"), HSCI(
 				"Health Sciences"), HTM("Hospitality & Tourism Mgmt"), IDE(
 				"Interdisciplinary Engr"), IDIS("Interdisciplinary Studies"), IE(
 				"Industrial Engineering"), IET("Industrial Engr Technology"), IPPH(
 				"Industrial & Phys Pharm"), IT("Industrial Technology"), ITAL(
 				"Italian"), JPNS("Japanese"), LA("Landscape Architecture"), LALS(
 				"Latina Am&Latino Studies"), LATN("Latin"), LCME(
 				"Lafayette Center Med Educ"), LING("Linguistics"), MA(
 				"Mathematics"), MARS("Medieval &Renaissance Std"), MCMP(
 				"Med Chem &Molecular Pharm"), ME("Mechanical Engineering"), MET(
 				"Mechanical Engr Tech"), MFET("Manufacturing Engr Tech"), MGMT(
 				"Management"), MSE("Materials Engineering"), MSL(
 				"Military Science & Ldrshp"), MUS("Music History & Theory"), NRES(
 				"Natural Res & Environ Sci"), NS("Naval Science"), NUCL(
 				"Nuclear Engineering"), NUPH("Nuclear Pharmacy"), NUR("Nursing"), NUTR(
 				"Nutrition Science"), OBHR("Orgnztnl Bhvr &Hum Resrce"), OLS(
 				"Organiz Ldrshp&Supervision"), PES("Physical Education Skills"), PHAD(
 				"Pharmacy Administration"), PHIL("Philosophy"), PHPR(
 				"Pharmacy Practice"), PHRM("Pharmacy"), PHYS("Physics"), POL(
 				"Political Science"), PSY("Psychology"), PTGS("Portuguese"), REG(
 				"Reg File Maintenance"), REL("Religious Studies"), RUSS(
 				"Russian"), SA("Study Abroad"), SCI("General Science"), SLHS(
 				"Speech, Lang&Hear Science"), SOC("Sociology"), SPAN("Spanish"), STAT(
 				"Statistics"), TECH("Technology"), THTR("Theatre"), USP(
 				"Undergrad Studies Prog"), VCS("Veterinary Clinical Sci"), VM(
 				"Veterinary Medicine"), WOST("Women's Studies"), YDAE(
				"Youth Develop & Ag Educ");
 
 		private final String fullName;
 
 		Subject(String fullName) {
 			this.fullName = fullName;
 		}
 
 		public String getFullName() {
 			return fullName;
 		}
 
 		@Override
 		public String toString() {
 			return fullName;
 		}
 	}
 
 	public enum Type {
 		DistanceLearning("Distance Learning", "DIS"), IndividualStudy(
 				"Individual Study", ""), Laboratory("Laboratory", "LAB"), Lecture(
 				"Lecture", "LEC"), Recitation("Recitation", "REC"), PracticeStudyObservation(
 				"Practice Study Observation", "PSO"), LaboratoryPreparation(
 				"Laboratory Preparation", ""), Experiential("Experiential", ""), Research(
 				"Research", ""), Studio("Studio", ""), Lab1("Lab1", "");
 
 		private final String name;
 		private final String linkName;
 
 		Type(String name, String linkName) {
 			this.name = name;
 			this.linkName = linkName;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public String getLinkName() {
 			return linkName;
 		}
 
 		@Override
 		public String toString() {
 			return name;
 		}
 	}
 }
