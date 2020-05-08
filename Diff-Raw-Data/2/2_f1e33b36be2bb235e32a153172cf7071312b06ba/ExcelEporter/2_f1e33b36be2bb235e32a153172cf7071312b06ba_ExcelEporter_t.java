 package ca.usask.gmcte.util;
 
 import java.io.File;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.TreeMap;
 
 import org.apache.log4j.Logger;
 
 import jxl.Workbook;
 import jxl.write.Label;
 import jxl.write.Number;
 import jxl.write.WritableCellFormat;
 import jxl.write.WritableFont;
 import jxl.write.WritableSheet;
 import jxl.write.WritableWorkbook;
 import jxl.write.WriteException;
 import jxl.write.biff.RowsExceededException;
 import ca.usask.gmcte.currimap.action.CourseManager;
 import ca.usask.gmcte.currimap.action.OrganizationManager;
 import ca.usask.gmcte.currimap.action.OutcomeManager;
 import ca.usask.gmcte.currimap.action.ProgramManager;
 import ca.usask.gmcte.currimap.action.QuestionManager;
 import ca.usask.gmcte.currimap.model.AnswerOption;
 import ca.usask.gmcte.currimap.model.AnswerSet;
 import ca.usask.gmcte.currimap.model.AssessmentFeedbackOption;
 import ca.usask.gmcte.currimap.model.AssessmentFeedbackOptionType;
 import ca.usask.gmcte.currimap.model.AssessmentTimeOption;
 import ca.usask.gmcte.currimap.model.Characteristic;
 import ca.usask.gmcte.currimap.model.CharacteristicType;
 import ca.usask.gmcte.currimap.model.Course;
 import ca.usask.gmcte.currimap.model.CourseAttribute;
 import ca.usask.gmcte.currimap.model.CourseAttributeValue;
 import ca.usask.gmcte.currimap.model.CourseOffering;
 import ca.usask.gmcte.currimap.model.LinkAssessmentCourseOutcome;
 import ca.usask.gmcte.currimap.model.LinkCourseContributionProgramOutcome;
 import ca.usask.gmcte.currimap.model.LinkCourseOfferingAssessment;
 import ca.usask.gmcte.currimap.model.LinkCourseOfferingContributionProgramOutcome;
 import ca.usask.gmcte.currimap.model.LinkCourseOfferingOutcome;
 import ca.usask.gmcte.currimap.model.LinkCourseOfferingTeachingMethod;
 import ca.usask.gmcte.currimap.model.LinkCourseOutcomeProgramOutcome;
 import ca.usask.gmcte.currimap.model.LinkCourseProgram;
 import ca.usask.gmcte.currimap.model.LinkProgramProgramOutcome;
 import ca.usask.gmcte.currimap.model.Organization;
 import ca.usask.gmcte.currimap.model.Program;
 import ca.usask.gmcte.currimap.model.ProgramOutcome;
 import ca.usask.gmcte.currimap.model.ProgramOutcomeGroup;
 import ca.usask.gmcte.currimap.model.Question;
 import ca.usask.gmcte.currimap.model.QuestionResponse;
 import ca.usask.gmcte.currimap.model.to.ProgramOutcomeCourseContribution;
 
 
 public class ExcelEporter
 {
 
 
 	public static File createExportFile(Organization organization) throws Exception
 	{
 		Logger logger = Logger.getLogger(ExcelEporter.class);
 			ResourceBundle bundle = ResourceBundle.getBundle("currimap");
 		String folderName = bundle.getString("tempFileFolder");
 		File tempFolder = new File(folderName);
 		tempFolder = new File(tempFolder, ""+System.currentTimeMillis());
 		tempFolder.mkdirs();
 		Calendar cal = Calendar.getInstance();
 		SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM_dd_yyyy_H_mm");
 		File file = new File(tempFolder,organization.getName().replaceAll(" ","_") +"_"+ dateFormatter.format(cal.getTime())+".xls");
 		WritableWorkbook workbook = Workbook.createWorkbook(file); 
 	
 		int sheetIndex = 0;
 		WritableSheet programsSheet = workbook.createSheet("Programs and Courses", sheetIndex++); 
 		
 		WritableSheet sheet = workbook.createSheet("Courses", sheetIndex++); 
 		WritableSheet offeringsSheet = workbook.createSheet("Offerings", sheetIndex++); 
 		
 
 		WritableSheet teachingMethodSheet = workbook.createSheet("Instructional strategies", sheetIndex++); 
 		
 		
 	
 		WritableSheet assessmentSheet = workbook.createSheet("Assessments", sheetIndex++); 
 		WritableSheet outcomesSheet = workbook.createSheet("Outcomes", sheetIndex++); 
 		WritableSheet assessmentToOutcomeSheet = workbook.createSheet("Assessment of Outcomes", sheetIndex++); 
 		WritableSheet courseWithinProgramSheet = workbook.createSheet("Course within Program", sheetIndex++); 
 		WritableSheet courseToProgramSheet = workbook.createSheet("Course OC -> Program OC", sheetIndex++); 
 		WritableSheet questionSheet = workbook.createSheet("Final Questions", sheetIndex++); 
 		try
 		{
 
 		CourseManager cm =  CourseManager.instance();
 		List<Course> homeCourses = cm.getCoursesForOrganization(organization);
 		Map<String,Course> homeCourseMapping = new TreeMap<String,Course>(); 
 		for(Course c: homeCourses)
 		{
 			homeCourseMapping.put(""+c.getId(), c);
 		}
 		WritableFont biggerFont = new WritableFont(WritableFont.ARIAL, 12,WritableFont.BOLD, false);
 		
 		WritableCellFormat biggerFormat = new WritableCellFormat (biggerFont); 
 		biggerFormat.setWrap(true);
 		
 		WritableCellFormat wrappedCell = new WritableCellFormat();
 		wrappedCell.setWrap(true);
 		
 		
 		
 		int col = 0;
 		int row = 0;
 		OrganizationManager om = OrganizationManager.instance();
 		
 		ProgramManager pm = ProgramManager.instance();
 		
 		Label parentOrgLabel = new Label(col++, row, "Parent Org",biggerFormat);
 		programsSheet.addCell(parentOrgLabel);
 		Label organizationLabel = new Label(col++, row, "Organization",biggerFormat);
 		programsSheet.addCell(organizationLabel);
 		Label programLabel = new Label(col++, row, "Program",biggerFormat);
 		programsSheet.addCell(programLabel);
 		Label programIdLabel = new Label(col++, row, "Program_Id",biggerFormat);
 		programsSheet.addCell(programIdLabel);
 	
 	
 		Label courseSubjectLabel = new Label(col++, row, "Course Subject",biggerFormat);
 		programsSheet.addCell(courseSubjectLabel);
 		Label courseNumberLabel = new Label(col++, row, "Course Number",biggerFormat);
 		programsSheet.addCell(courseNumberLabel);
 		Label courseIdLabel = new Label(col++, row, "course_id",biggerFormat);
 		programsSheet.addCell(courseIdLabel);
 		Label classificationLabel = new Label(col++, row, "Classification",biggerFormat);
 		programsSheet.addCell(classificationLabel);
 		Label whenInProgramLabel = new Label(col++, row, "When in Program",biggerFormat);
 		programsSheet.addCell(whenInProgramLabel);
 		Label homeOrServiceLabel = new Label(col++, row, "Home or Service",biggerFormat);
 		programsSheet.addCell(homeOrServiceLabel);
 		
 		
 		List<LinkCourseProgram> programLinks = pm.getAllLinkCourseProgramForOrganization(organization);
 		// set columns to 30 chars
 		for(int i = 0; i< 10; i++)
 		{
 			programsSheet.setColumnView(i,  30);
 		}
 		row++;
 		for(LinkCourseProgram p : programLinks)
 		{
 			col=0;
 			Organization o = p.getProgram().getOrganization();
 			if (o.getParentOrganization() !=null)
 			{
 				Label parentOrgValueLabel = new Label(col, row, ""+o.getParentOrganization().getName(),wrappedCell);
 				programsSheet.addCell(parentOrgValueLabel);
 			}
 			col++;
 			Label orgValueLabel = new Label(col++, row, o.getName(),wrappedCell);
 			programsSheet.addCell(orgValueLabel);
 			Label progValueLabel = new Label(col++, row, p.getProgram().getName(),wrappedCell);
 			programsSheet.addCell(progValueLabel);
 			Label progIdValueLabel = new Label(col++, row, ""+p.getProgram().getId(),wrappedCell);
 			programsSheet.addCell(progIdValueLabel);
 			Label courseSubjectValueLabel = new Label(col++, row, p.getCourse().getSubject(),wrappedCell);
 			programsSheet.addCell(courseSubjectValueLabel);
 			
 			Label courseNumberValueLabel = new Label(col++, row, ""+p.getCourse().getCourseNumber(),wrappedCell);
 			programsSheet.addCell(courseNumberValueLabel);
 			Label courseIdValueLabel = new Label(col++, row, ""+p.getCourse().getId(),wrappedCell);
 			programsSheet.addCell(courseIdValueLabel);
 			Label courseClassificationValueLabel = new Label(col++, row, p.getCourseClassification().getName(),wrappedCell);
 			programsSheet.addCell(courseClassificationValueLabel);
 			Label whenInProgramValueLabel = new Label(col++, row, p.getTime().getName(),wrappedCell);
 			programsSheet.addCell(whenInProgramValueLabel);
 			Label homeOfServiceValueLabel = new Label(col++, row, homeCourseMapping.containsKey(""+p.getCourse().getId())?"Home course":"Service Course",wrappedCell);
 			programsSheet.addCell(homeOfServiceValueLabel);
 			row++;
 			
 		}
 		
 		
 		
 		
 		
 		
 		
 		List<Course> coursesLinkedToOrganization = om.getAllCourses(organization);
 		
 		
 	/*	//main labels
 		String[] labels = {"Subject", "Course Number"};
 		int[] mainColumns = {0,1};
 	
 		for(int i = 0; i< labels.length; i++)
 		{
 			Label labelToAdd = new Label(mainColumns[i], row, labels[i],biggerFormat);
 			sheet.addCell(labelToAdd);
 		}
 		//merge header main header cells
 	//	sheet.mergeCells(mainColumns[2], row, mainColumns[3]-1, row);
 	//	sheet.mergeCells(mainColumns[3], row, mainColumns[4]-1, row);
 		*/
 		
 		Label orgLabel = new Label(0, 0, organization.getName(),biggerFormat);
 		sheet.addCell(orgLabel);
 	
 		row = 3;
 		//add secondary headers
 		row++;
 		col=0;
 		courseSubjectLabel = new Label(col++, row, "Subject",biggerFormat);
 		sheet.addCell(courseSubjectLabel);
 		courseNumberLabel = new Label(col++, row, "Number",biggerFormat);
 		sheet.addCell(courseNumberLabel);
 		courseIdLabel = new Label(col++, row, "course_id",biggerFormat);
 		sheet.addCell(courseIdLabel);
 		List<String> courseIds = new ArrayList<String>();
 		for(int i = 0; i< 4; i++)
 		{
 			sheet.setColumnView(i,  30);
 		}
 		row++;
 		col=0;
 		for (Course c : coursesLinkedToOrganization)
 		{
 			Label subjectLabel = new Label(col++, row, c.getSubject(),wrappedCell);
 			sheet.addCell(subjectLabel);
 			Label numberLabel = new Label(col++, row, ""+c.getCourseNumber(),wrappedCell);
 			sheet.addCell(numberLabel);
 			Label idLabel = new Label(col++, row, ""+c.getId(),wrappedCell);
 			sheet.addCell(idLabel);
 			courseIds.add(""+c.getId());
 			row++;
 			col=0;
 		}
 	
 		List<CourseOffering> offerings = cm.getCourseOfferingsForCourses(courseIds);
 		
 		for(int i = 0; i< 10; i++)
 		{
 			offeringsSheet.setColumnView(i,  30);
 		}
 		
 		
 		
 		
 		row=0;
 		col=0;
 		courseIdLabel = new Label(col++, row, "course_id",biggerFormat);
 		offeringsSheet.addCell(courseIdLabel);
 		Label termLabel = new Label(col++, row, "Term",biggerFormat);
 		offeringsSheet.addCell(termLabel);
 		Label sectionNumberLabel = new Label(col++, row, "Section number",biggerFormat);
 		offeringsSheet.addCell(sectionNumberLabel);
 		Label mediumLabel = new Label(col++, row, "Medium",biggerFormat);
 		offeringsSheet.addCell(mediumLabel);
 		
 		
 		Label instrLabel= new Label(col++, row, "Instructor(s)",biggerFormat);
 		offeringsSheet.addCell(instrLabel);
 		Label numStudentsLabel = new Label(col++, row, "Num students",biggerFormat);
 		offeringsSheet.addCell(numStudentsLabel);
 		Label completionTimeLabel = new Label(col++, row, "Completion time",biggerFormat);
 		offeringsSheet.addCell(completionTimeLabel);
 		Label completionValueLabel = new Label(col++, row, "Completion time value",biggerFormat);
 		offeringsSheet.addCell(completionValueLabel);
 		Label commentsLabel = new Label(col++, row, "Comments",biggerFormat);
 		offeringsSheet.addCell(commentsLabel);
 		Label courseOfferingIdLabel = new Label(col++, row, "course_offering_id",biggerFormat);
 		offeringsSheet.addCell(courseOfferingIdLabel);
 		row++;
 		col=0;
 		for (CourseOffering co : offerings)
 		{
 			Label courseLabel = new Label(col++, row, ""+co.getCourse().getId());
 			offeringsSheet.addCell(courseLabel);
 			
 			Label coTermLabel = new Label(col++, row, co.getTerm(),wrappedCell);
 			offeringsSheet.addCell(coTermLabel);
 			Label coSectionNumLabel = new Label(col++, row, ""+co.getSectionNumber(),wrappedCell);
 			offeringsSheet.addCell(coSectionNumLabel);
 			Label medLabel = new Label(col++, row, ""+co.getMedium(),wrappedCell);
 			offeringsSheet.addCell(medLabel);
 			Label instructorsLabel = new Label(col++, row, cm.getInstructorsString(co, false, "-2",false),wrappedCell);
 			offeringsSheet.addCell(instructorsLabel);
 			Label studentsLabel = new Label(col++, row, ""+co.getNumStudents(),wrappedCell);
 			offeringsSheet.addCell(studentsLabel);
 			Label completionLabel = new Label(col++, row, co.getTimeItTook()!=null?co.getTimeItTook().getName():"",wrappedCell);
 			offeringsSheet.addCell(completionLabel);
 			Label completionCalcLabel = new Label(col++, row, co.getTimeItTook()!=null?""+co.getTimeItTook().getCalculationValue():"",wrappedCell);
 			offeringsSheet.addCell(completionCalcLabel);
 			Label commentLabel = new Label(col++, row, co.getComments()!=null?co.getComments():"", wrappedCell);
 			offeringsSheet.addCell(commentLabel);
 			Label idLabel = new Label(col++, row, ""+co.getId(),wrappedCell);
 			offeringsSheet.addCell(idLabel);
 			row++;
 			col=0;
 		}
 		
 
 		
 		//TEACHING METHODS
 		List<LinkCourseOfferingTeachingMethod> methods = cm.getTeachingMethods(courseIds);
 		
 		row=0;
 		col=0;
 		courseIdLabel = new Label(col++, row, "course_id",biggerFormat);
 		teachingMethodSheet.addCell(courseIdLabel);
 		courseOfferingIdLabel = new Label(col++, row, "course_offering_id",biggerFormat);
 		teachingMethodSheet.addCell(courseOfferingIdLabel);
 		
 		Label stratLabel= new Label(col++, row, "Instructional strategy",biggerFormat);
 		teachingMethodSheet.addCell(stratLabel);
 		Label extentLabel = new Label(col++, row, "Extent of Use",biggerFormat);
 		teachingMethodSheet.addCell(extentLabel);
 		Label extentValueLabel = new Label(col++, row, "Extent of Use value",biggerFormat);
 		teachingMethodSheet.addCell(extentValueLabel);
 		
 		for(int i = 0; i< 5; i++)
 		{
 			teachingMethodSheet.setColumnView(i,  30);
 		}
 		row++;
 		col=0;
 		for (LinkCourseOfferingTeachingMethod meth : methods)
 		{
 			Label courseLabel = new Label(col++, row, ""+meth.getCourseOffering().getCourse().getId());
 			teachingMethodSheet.addCell(courseLabel);
 			
 			Label idLabel = new Label(col++, row, ""+meth.getCourseOffering().getId(),wrappedCell);
 			teachingMethodSheet.addCell(idLabel);
 			Label strategyLabel = new Label(col++, row, ""+meth.getTeachingMethod().getName(),wrappedCell);
 			teachingMethodSheet.addCell(strategyLabel);
 			Label extLabel = new Label(col++, row, ""+meth.getHowLong().getName(),wrappedCell);
 			teachingMethodSheet.addCell(extLabel);
 			Label extValLabel = new Label(col++, row, ""+meth.getHowLong().getComparativeValue(),wrappedCell);
 			teachingMethodSheet.addCell(extValLabel);
 			row++;
 			col=0;
 		}
 		
 		
 		//ASSESSMENT METHODS
 		List<LinkCourseOfferingAssessment> assessmentList = cm.getAssessmentsForCourses(courseIds);
 
 		List<AssessmentTimeOption> timeOptionsList = cm.getAssessmentTimeOptions();
 
 		List<String> timeOptions = new ArrayList<String>();
 
 		for(AssessmentTimeOption time : timeOptionsList)
 		{	
 			timeOptions.add(time.getName());
 		}
 		NumberFormat formatter = NumberFormat.getInstance();
 		formatter.setMaximumFractionDigits(1);
 
 		row=0;
 		col=0;
 		courseIdLabel = new Label(col++, row, "course_id",biggerFormat);
 		assessmentSheet.addCell(courseIdLabel);
 		courseOfferingIdLabel = new Label(col++, row, "course_offering_id",biggerFormat);
 		assessmentSheet.addCell(courseOfferingIdLabel);
 		Label assessmentIdLabel = new Label(col++, row, "Assessment id",biggerFormat);
 		assessmentSheet.addCell(assessmentIdLabel);
 		Label assGrpLabel= new Label(col++, row, "Group",biggerFormat);
 		assessmentSheet.addCell(assGrpLabel);
 		Label assNameLabel = new Label(col++, row, "Name",biggerFormat);
 		assessmentSheet.addCell(assNameLabel);
 		Label addInfoLabel = new Label(col++, row, "Addnl Info",biggerFormat);
 		assessmentSheet.addCell(addInfoLabel);
 		Label assWeightLabel = new Label(col++, row, "Weight",biggerFormat);
 		assessmentSheet.addCell(assWeightLabel);
 		Label assWhenLabel = new Label(col++, row, "When",biggerFormat);
 		assessmentSheet.addCell(assWhenLabel);
 		Label assCritLabel = new Label(col++, row, "Criterion",biggerFormat);
 		assessmentSheet.addCell(assCritLabel);
 		Label assCritLvlLabel = new Label(col++, row, "Crit Level",biggerFormat);
 		assessmentSheet.addCell(assCritLvlLabel);
 		Label assCritComplLabel = new Label(col++, row, "Crit complete",biggerFormat);
 		assessmentSheet.addCell(assCritComplLabel);
 		Label assCritSubmLabel = new Label(col++, row, "Crit submit",biggerFormat);
 		assessmentSheet.addCell(assCritSubmLabel);
 	
 		TreeMap<String,List<AssessmentFeedbackOption>> additionalInfoOptions = new TreeMap<String,List<AssessmentFeedbackOption>>();
 		
 		List<AssessmentFeedbackOptionType> questions = cm.getAssessmentFeedbackQuestions();
 		for(AssessmentFeedbackOptionType q : questions)
 		{
 			List<AssessmentFeedbackOption> options = cm.getAssessmentOptionsForQuestion(q.getId());
 			additionalInfoOptions.put(""+q.getId(), options);
 			String type = q.getQuestionType(); //"select","checkbox","radio"
 			Label questionNameLabel = new Label(col++, row, q.getQuestion(), wrappedCell);
 			assessmentSheet.addCell(questionNameLabel);
 			if(type.equals("checkbox")) //there could be multiple options
 			{
 				
 				col--;
 				assessmentSheet.mergeCells(col,row, col+options.size()-1,row);
 				
 				for(AssessmentFeedbackOption option : options)
 				{
 					Label questionOptionLabel = new Label(col++, row+1, option.getName(), wrappedCell);
 					assessmentSheet.addCell(questionOptionLabel);	
 				}	
 			}
 		}
 		
 		for(int i = 0; i< 5; i++)
 		{
 			assessmentSheet.setColumnView(i,  30);
 		}
 		for(int i = 5; i< 10; i++)
 		{
 			assessmentSheet.setColumnView(i,  15);
 		}
 		row+=2;
 		
 		
 		for (LinkCourseOfferingAssessment item : assessmentList)
 		{
 			col=0;
 			Label courseLabel = new Label(col++, row, ""+item.getCourseOffering().getCourse().getId());
 			assessmentSheet.addCell(courseLabel);
 			
 			Label idLabel = new Label(col++, row, ""+item.getCourseOffering().getId(),wrappedCell);
 			assessmentSheet.addCell(idLabel);
 			Label assessmentIdVal = new Label(col++, row, ""+item.getAssessment().getId(),wrappedCell);
 			assessmentSheet.addCell(assessmentIdVal);
 			
 			Label group = new Label(col++, row, ""+item.getAssessment().getGroup().getShortName(),wrappedCell);
 			assessmentSheet.addCell(group);
 			Label name = new Label(col++, row, ""+item.getAssessment().getName(),wrappedCell);
 			assessmentSheet.addCell(name);
 			Label additionalInfo = new Label(col++, row, HTMLTools.isValid(item.getAdditionalInfo())?item.getAdditionalInfo():"", wrappedCell);
 			assessmentSheet.addCell(additionalInfo);
 			
 			Label weight = new Label(col++, row, ""+item.getWeight(), wrappedCell);
 			assessmentSheet.addCell(weight);
 			Label when = new Label(col++, row, ""+item.getWhen().getName(), wrappedCell);
 			assessmentSheet.addCell(when);
 			String criterion = item.getCriterionExists();
 			Label criterionLabel =  new Label(col++, row, criterion, wrappedCell);
 			assessmentSheet.addCell(criterionLabel);
 			
 			if(criterion.equalsIgnoreCase("Y"))
 			{
 				Label criterionLevel = new Label(col++, row, ""+item.getCriterionLevel(), wrappedCell);
 				assessmentSheet.addCell(criterionLevel);
 				Label criterionCompl = new Label(col++, row, ""+item.getCriterionCompleted(), wrappedCell);
 				assessmentSheet.addCell(criterionCompl);
 				Label criterionSubm = new Label(col++, row, ""+item.getCriterionSubmitted(), wrappedCell);
 				assessmentSheet.addCell(criterionSubm);
 			}
 			else
 			{
 				col+=3;
 			}
 			
 			List<AssessmentFeedbackOption> selectedOptions = cm.getAssessmentOptionsSelectedForLinkOffering(item.getId());
 
 			TreeMap<String ,AssessmentFeedbackOption> optionIdMapping = new TreeMap<String ,AssessmentFeedbackOption>();
 			for(AssessmentFeedbackOption selectedOption: selectedOptions )
 			{
 				optionIdMapping.put(""+selectedOption.getId(),selectedOption);
 			}
 			for(AssessmentFeedbackOptionType q : questions)
 			{
 				List<AssessmentFeedbackOption> options = additionalInfoOptions.get(""+q.getId());
 				String type = q.getQuestionType(); //"select","checkbox","radio"
 				if(type.equals("select") || type.equals("radio") )
 				{
 					for(AssessmentFeedbackOption option : options)
 					{
 						if(optionIdMapping.containsKey(""+option.getId()))
 						{
 							Label optionResponseLabel = new Label(col, row, option.getName(), wrappedCell);
 							assessmentSheet.addCell(optionResponseLabel);
 						
 						}
 					}
 					col++;
 				}
 				else
 				{
 					for(AssessmentFeedbackOption option : options)
 					{
 						if(optionIdMapping.containsKey(""+option.getId()))
 						{
 							Label questionOptionLabel = new Label(col++, row, "1", wrappedCell);
 							assessmentSheet.addCell(questionOptionLabel);	
 						}
 						else
 						{
 							Label questionOptionLabel = new Label(col++, row, "0", wrappedCell);
 							assessmentSheet.addCell(questionOptionLabel);	
 						}
 					}	
 				}
 			}
 			row++;
 
 		}
 		col=0;
 		
 		//COURSE OUTCOMES
 		row=0;
 		col=0;
 		courseIdLabel = new Label(col++, row, "course_id",biggerFormat);
 		outcomesSheet.addCell(courseIdLabel);
 		courseOfferingIdLabel = new Label(col++, row, "course_offering_id",biggerFormat);
 		outcomesSheet.addCell(courseOfferingIdLabel);
 	
 		Label outcomeLabel= new Label(col++, row, "Course Outcome",biggerFormat);
 		outcomesSheet.addCell(outcomeLabel);
 		
 		Label outcomeIdLabel= new Label(col++, row, "Course Outcome ID",biggerFormat);
 		outcomesSheet.addCell(outcomeIdLabel);
 		
 		
 		List<CharacteristicType> characteristicTypes = organization.getCharacteristicTypes();
 		for(CharacteristicType charType: characteristicTypes)
 		{
 			Label charTypeQuestionLabel= new Label(col++, row,charType.getQuestionDisplay(),wrappedCell);
 			outcomesSheet.addCell(charTypeQuestionLabel);
 		}
 		// set columns to 30 chars
 		for(int i = 0; i< col; i++)
 		{
 			outcomesSheet.setColumnView(i,  30);
 		}
 		OutcomeManager outcomeManager = OutcomeManager.instance();
 		List<LinkCourseOfferingOutcome> outcomes = outcomeManager.getOutcomesForCourses(courseIds);
 		row++;
 		
 		for(LinkCourseOfferingOutcome oLink : outcomes)
 		{
 			col=0;
 			Label courseLabel = new Label(col++, row, ""+oLink.getCourseOffering().getCourse().getId());
 			outcomesSheet.addCell(courseLabel);
 			
 			Label idLabel = new Label(col++, row, ""+oLink.getCourseOffering().getId(),wrappedCell);
 			outcomesSheet.addCell(idLabel);
 			Label outcomeTextLabel= new Label(col++, row, oLink.getCourseOutcome().getName(),wrappedCell);
 			outcomesSheet.addCell(outcomeTextLabel);
 			Label outIdLabel = new Label(col++, row, ""+oLink.getCourseOutcome().getId(),wrappedCell);
 			outcomesSheet.addCell(outIdLabel);
 		
 			List<Characteristic> outcomeCharacteristics = outcomeManager.getCharacteristicsForCourseOfferingOutcome(oLink.getCourseOffering(),oLink.getCourseOutcome(), organization);
 				
 			for(CharacteristicType charType: characteristicTypes)
 			{
 				
 				boolean found = false;
 				for (Characteristic c : outcomeCharacteristics)
 				{
 					if(c.getCharacteristicType().getId() == charType.getId())
 					{
 						Label charTypeValueLabel= new Label(col++, row,c.getName(),wrappedCell);
 						outcomesSheet.addCell(charTypeValueLabel);
 						found = true;
 					}
 				}
 				if(!found)
 				{
 					Label charTypeValueLabel= new Label(col++, row,"not known",wrappedCell);
 					outcomesSheet.addCell(charTypeValueLabel);
 				}
 			}
 			row++;
 		}
 		
 		//Mapping of COURSE OUTCOMES to Assessment
 		row=0;
 		col=0;
 		courseIdLabel = new Label(col++, row, "course_id",biggerFormat);
 		assessmentToOutcomeSheet.addCell(courseIdLabel);
 		courseOfferingIdLabel = new Label(col++, row, "course_offering_id",biggerFormat);
 		assessmentToOutcomeSheet.addCell(courseOfferingIdLabel);
 	
 		assessmentIdLabel= new Label(col++, row, "Assessment ID",biggerFormat);
 		assessmentToOutcomeSheet.addCell(assessmentIdLabel);
 		
 		outcomeIdLabel= new Label(col++, row, "Course Outcome ID",biggerFormat);
 		
 		// set columns to 30 chars
 		for(int i = 0; i< 4; i++)
 		{
 			assessmentToOutcomeSheet.setColumnView(i,  30);
 		}
 			
 		row++;
 		assessmentToOutcomeSheet.addCell(outcomeIdLabel);
 		List<LinkAssessmentCourseOutcome> existingLinks = outcomeManager.getLinkAssessmentCourseOutcomesForCourses(courseIds);
 		for(LinkAssessmentCourseOutcome link : existingLinks)
 		{
 			col=0;
 			Label courseLabel = new Label(col++, row, ""+link.getCourseOffering().getCourse().getId());
 			assessmentToOutcomeSheet.addCell(courseLabel);
 			
 			Label idLabel = new Label(col++, row, ""+link.getCourseOffering().getId(),wrappedCell);
 			assessmentToOutcomeSheet.addCell(idLabel);
 			
 			Label assessmentIdTextLabel= new Label(col++, row, ""+link.getAssessmentLink().getAssessment().getId(),wrappedCell);
 			assessmentToOutcomeSheet.addCell(assessmentIdTextLabel);
 			Label outIdLabel = new Label(col++, row, ""+link.getOutcome().getId(),wrappedCell);
 			assessmentToOutcomeSheet.addCell(outIdLabel);
 			row++;
 		}
 		
 		
 		//Mapping of emphasis and depth to program outcomes
 		row=0;
 		col=0;
 		Label programOutcomeGroupLabel = new Label(col++, row, "Program Outcome Group",biggerFormat);
 		courseWithinProgramSheet.addCell(programOutcomeGroupLabel);
 		Label programOutcomeLabel = new Label(col++, row, "Program Outcome",biggerFormat);
 		courseWithinProgramSheet.addCell(programOutcomeLabel);
 		programIdLabel = new Label(col++, row, "program_id",biggerFormat);
 		courseWithinProgramSheet.addCell(programIdLabel);
 		
 		courseIdLabel = new Label(col++, row, "course_id",biggerFormat);
 		courseWithinProgramSheet.addCell(courseIdLabel);
 		courseOfferingIdLabel = new Label(col++, row, "course_offering_id",biggerFormat);
 		courseWithinProgramSheet.addCell(courseOfferingIdLabel);
 	
 		Label emphasisNameLabel= new Label(col++, row, "Emphasis",biggerFormat);
 		courseWithinProgramSheet.addCell(emphasisNameLabel);
 		Label emphasisIdLabel= new Label(col++, row, "Emphasis value",biggerFormat);
 		courseWithinProgramSheet.addCell(emphasisIdLabel);
 		Label depthNameLabel= new Label(col++, row, "Depth",biggerFormat);
 		courseWithinProgramSheet.addCell(depthNameLabel);
 		Label depthIdLabel= new Label(col++, row, "Depth value",biggerFormat);
 		courseWithinProgramSheet.addCell(depthIdLabel);
 		homeOrServiceLabel = new Label(col++, row, "Home or Service",biggerFormat);
 		courseWithinProgramSheet.addCell(homeOrServiceLabel);
 		
 		// set columns to 30 chars
 		for(int i = 0; i< 9; i++)
 		{
 			courseWithinProgramSheet.setColumnView(i,  30);
 		}
 		row++;
 		List<ProgramOutcomeGroup> groups = pm.getProgramOutcomeGroupsOrganization(organization);
 		
 		for(ProgramOutcomeGroup group : groups)
 		{
 			List<LinkProgramProgramOutcome> programOutcomes = pm.getProgramOutcomeForGroup(group);
 			
 			for(LinkProgramProgramOutcome programOutcomeLink: programOutcomes)
 			{
 				
 				ProgramOutcome programOutcome = programOutcomeLink.getProgramOutcome();
 				List<LinkCourseOfferingContributionProgramOutcome> contributionLinks = pm.getCourseOfferingContributionLinksForProgramOutcome(courseIds,programOutcomeLink);
 				for(LinkCourseOfferingContributionProgramOutcome link : contributionLinks)
 				{
 					col = 0;
 					//programOutcomeGroup
 					Label groupValueLabel = new Label(col++, row, group.getName(),wrappedCell);
 					courseWithinProgramSheet.addCell(groupValueLabel);
 			
 					//programOutcome
 					Label outcomeValueLabel = new Label(col++, row, programOutcome.getName(),wrappedCell);
 					courseWithinProgramSheet.addCell(outcomeValueLabel);
 			
 					
 					//programId
 					Label programValueLabel = new Label(col++, row, ""+programOutcomeLink.getProgram().getId(),wrappedCell);
 					courseWithinProgramSheet.addCell(programValueLabel);
 					//course
 
 					Label courseValueLabel = new Label(col++, row, ""+link.getCourseOffering().getCourse().getId(),wrappedCell);
 					courseWithinProgramSheet.addCell(courseValueLabel);
 					//courseOffering
 					Label courseOfferingValueLabel = new Label(col++, row, ""+link.getCourseOffering().getId(),wrappedCell);
 					courseWithinProgramSheet.addCell(courseOfferingValueLabel);
 					
 					//emphasis
 					Label emphasisValueLabel = new Label(col++, row, link.getContribution().getName(),wrappedCell);
 					courseWithinProgramSheet.addCell(emphasisValueLabel);
 					Label emphasisIdValueLabel = new Label(col++, row, ""+link.getContribution().getCalculationValue(),wrappedCell);
 					courseWithinProgramSheet.addCell(emphasisIdValueLabel);
 				
 					//depth
 					Label depthValueLabel = new Label(col++, row, link.getMastery().getName(),wrappedCell);
 					courseWithinProgramSheet.addCell(depthValueLabel);
 					Label depthIdValueLabel = new Label(col++, row, ""+link.getMastery().getCalculationValue(),wrappedCell);
 					courseWithinProgramSheet.addCell(depthIdValueLabel);
 					Label homeOfServiceValueLabel = new Label(col++, row, "Home course",wrappedCell);
 					courseWithinProgramSheet.addCell(homeOfServiceValueLabel);
 					row++;
 				}
 				List<LinkCourseContributionProgramOutcome> serviceContributionLinks = pm.getCourseContributionLinksForProgramOutcome(courseIds,programOutcomeLink);
 				for(LinkCourseContributionProgramOutcome link : serviceContributionLinks)
 				{
 					col = 0;
 					//programOutcomeGroup
 					Label groupValueLabel = new Label(col++, row, group.getName(),wrappedCell);
 					courseWithinProgramSheet.addCell(groupValueLabel);
 			
 					//programOutcome
 					Label outcomeValueLabel = new Label(col++, row, programOutcome.getName(),wrappedCell);
 					courseWithinProgramSheet.addCell(outcomeValueLabel);
 			
 					
 					//programId
 					Label programValueLabel = new Label(col++, row, ""+programOutcomeLink.getProgram().getId(),wrappedCell);
 					courseWithinProgramSheet.addCell(programValueLabel);
 					//course
 
 					Label courseValueLabel = new Label(col++, row, ""+link.getCourse().getId(),wrappedCell);
 					courseWithinProgramSheet.addCell(courseValueLabel);
 					//courseOffering
 					Label courseOfferingValueLabel = new Label(col++, row, "",wrappedCell);
 					courseWithinProgramSheet.addCell(courseOfferingValueLabel);
 					
 					//emphasis
 					Label emphasisValueLabel = new Label(col++, row, link.getContribution().getName(),wrappedCell);
 					courseWithinProgramSheet.addCell(emphasisValueLabel);
 					Label emphasisIdValueLabel = new Label(col++, row, ""+link.getContribution().getCalculationValue(),wrappedCell);
 					courseWithinProgramSheet.addCell(emphasisIdValueLabel);
 				
 					//depth
 					Label depthValueLabel = new Label(col++, row, link.getMastery().getName(),wrappedCell);
 					courseWithinProgramSheet.addCell(depthValueLabel);
 					Label depthIdValueLabel = new Label(col++, row, ""+link.getMastery().getCalculationValue(),wrappedCell);
 					courseWithinProgramSheet.addCell(depthIdValueLabel);
 					Label homeOfServiceValueLabel = new Label(col++, row,"Service Course",wrappedCell);
 					courseWithinProgramSheet.addCell(homeOfServiceValueLabel);
 					row++;
 				}
 			}
 		}
 		
 		//Mapping of COURSE OUTCOMES to Program Outcomes
 		row=0;
 		col=0;
 		programOutcomeGroupLabel = new Label(col++, row, "Program Outcome Group",biggerFormat);
 		courseToProgramSheet.addCell(programOutcomeGroupLabel);
 		programOutcomeLabel = new Label(col++, row, "Program Outcome",biggerFormat);
 		courseToProgramSheet.addCell(programOutcomeLabel);
 		programIdLabel = new Label(col++, row, "program_id",biggerFormat);
 		courseToProgramSheet.addCell(programIdLabel);
 		
 		courseIdLabel = new Label(col++, row, "course_id",biggerFormat);
 		courseToProgramSheet.addCell(courseIdLabel);
 		courseOfferingIdLabel = new Label(col++, row, "course_offering_id",biggerFormat);
 		courseToProgramSheet.addCell(courseOfferingIdLabel);
 	
 		Label outcomeIdHeaderLabel= new Label(col++, row, "Course Outcome_ID",biggerFormat);
 		courseToProgramSheet.addCell(outcomeIdHeaderLabel);
 		
 		
 		// set columns to 30 chars
 		for(int i = 0; i< 6; i++)
 		{
 			courseToProgramSheet.setColumnView(i,  30);
 		}
 		row++;
 	
 		for(ProgramOutcomeGroup group : groups)
 		{
 			List<LinkProgramProgramOutcome> programOutcomes = pm.getProgramOutcomeForGroup(group);
 			
 			for(LinkProgramProgramOutcome programOutcomeLink: programOutcomes)
 			{
 				
 				ProgramOutcome programOutcome = programOutcomeLink.getProgramOutcome();
 				
 				List<LinkCourseOutcomeProgramOutcome> links = pm.getCourseOutcomeLinksForProgramOutcome(courseIds, programOutcome);
 				
 				for(LinkCourseOutcomeProgramOutcome link : links)
 				{
 					col = 0;
 					//programOutcomeGroup
 					Label groupValueLabel = new Label(col++, row, group.getName(),wrappedCell);
 					courseToProgramSheet.addCell(groupValueLabel);
 			
 					//programOutcome
 					Label outcomeValueLabel = new Label(col++, row, programOutcome.getName(),wrappedCell);
 					courseToProgramSheet.addCell(outcomeValueLabel);
 			
 					
 					//programId
 					Label programValueLabel = new Label(col++, row, ""+programOutcomeLink.getProgram().getId(),wrappedCell);
 					courseToProgramSheet.addCell(programValueLabel);
 					//course
 
 					Label courseValueLabel = new Label(col++, row, ""+link.getCourseOffering().getCourse().getId(),wrappedCell);
 					courseToProgramSheet.addCell(courseValueLabel);
 					//courseOffering
 					Label courseOfferingValueLabel = new Label(col++, row, ""+link.getCourseOffering().getId(),wrappedCell);
 					courseToProgramSheet.addCell(courseOfferingValueLabel);
 					
 					//Outcome
 					Label outcomeIDValueLabel = new Label(col++, row, ""+link.getCourseOutcome().getId(),wrappedCell);
 					courseToProgramSheet.addCell(outcomeIDValueLabel);
 					
 					row++;
 				}
 				
 			}
 		}
 		
 		
 		
 		//Final Questions
 		row=0;
 		col=0;
 		
 		programIdLabel = new Label(col++, row, "program_id",biggerFormat);
 		questionSheet.addCell(programIdLabel);
 				
 		courseOfferingIdLabel = new Label(col++, row, "course_offering_id",biggerFormat);
 		questionSheet.addCell(courseOfferingIdLabel);
 			
 		QuestionManager qm = QuestionManager.instance();
 		List<Program> programs = pm.getAllProgramsForOrganization(organization);
 		int maxQuestions = 0;
 		for(Program program : programs)
 		{
 			col = 2;
 		
 			List<Question> programQuestions	= qm.getAllQuestionsForProgram(program);
 			int[] questionIds = new int[programQuestions.size()];
 			int i = 0;
 			for (Question q : programQuestions)
 			{
 				Label questionLabel = new Label(col++, row, q.getDisplay(),wrappedCell);
 				questionSheet.addCell(questionLabel);
 				questionIds[i++] = q.getId();
 			}
 			if(programQuestions.size() > maxQuestions)
 				maxQuestions = programQuestions.size();
 			
 			row++;
 			col = 0;
 			List<QuestionResponse> responses = qm.getAllQuestionResponsesForProgram(program);
 			for(QuestionResponse response: responses)
 			{
				col = 0;
 				Label programIdValueLabel = new Label(col++, row, ""+program.getId(),wrappedCell);
 				questionSheet.addCell(programIdValueLabel);
 				Label courseOfferingIdValueLabel = new Label(col++, row, ""+response.getCourseOffering().getId(),wrappedCell);
 				questionSheet.addCell(courseOfferingIdValueLabel);
 				int index = findIndex(questionIds, response.getQuestion().getId());
 				if(index > -1)
 				{
 					String value = response.getResponse();
 					if(response.getQuestion().getAnswerSet() != null)
 						value = getDisplayValue(response.getQuestion().getAnswerSet(), value);
 					
 					Label responseValueLabel = new Label(index+2, row, value,wrappedCell);
 					questionSheet.addCell(responseValueLabel);
 				}
 				else
 					logger.error("Unable to find question with ID "+response.getQuestion().getId() + " for program "+program.getId());
 				row++;
 			}
 		}
 			
 		// set columns to 30 chars
 		for(int i = 0; i< maxQuestions+2; i++)
 		{
 			questionSheet.setColumnView(i,  30);
 		}	
 		}
 		catch(Exception e)
 		{
 			logger.error("Oops...",e);
 		}
 		workbook.write();
 		workbook.close(); 
 		return file;
 	}
 	private static String getDisplayValue(AnswerSet set, String value)
 	{
 		for(AnswerOption option : set.getAnswerOptions())
 		{
 			if(option.getValue().equals(value))
 				return option.getDisplay();
 		}
 		return value;
 	}
 	private static int findIndex(int[] a, int toFind)
 	{
 		for(int i = 0; i < a.length ; i++)
 		{
 			if(a[i] == toFind)
 				return i;
 		}
 		return -1;
 	}
 	
 	
 	public static File createExcelFile(Program program) throws Exception
 	{
 		
 		ResourceBundle bundle = ResourceBundle.getBundle("currimap");
 		String folderName = bundle.getString("tempFileFolder");
 		File tempFolder = new File(folderName);
 		tempFolder = new File(tempFolder, ""+System.currentTimeMillis());
 		tempFolder.mkdirs();
 		Calendar cal = Calendar.getInstance();
 		SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM_dd_yyyy_H_mm");
 		File file = new File(tempFolder,program.getName() +"_"+ dateFormatter.format(cal.getTime())+".xls");
 		WritableWorkbook workbook = Workbook.createWorkbook(file); 
 	
 		WritableSheet sheet = workbook.createSheet("First Sheet", 0); 
 		
 		WritableFont biggerFont = new WritableFont(WritableFont.ARIAL, 14,WritableFont.BOLD, false);
 		
 		WritableCellFormat biggerFormat = new WritableCellFormat (biggerFont); 
 		biggerFormat.setWrap(true);
 		
 		WritableCellFormat wrappedCell = new WritableCellFormat();
 		wrappedCell.setWrap(true);
 		
 		int col = 0;
 		int row = 3;
 		Organization organization = program.getOrganization();
 		if(organization.getParentOrganization() != null)
 			organization = organization.getParentOrganization();
 		
 		List<CourseAttribute> courseAttributes = OrganizationManager.instance().getCourseAttributes(organization);
 		
 		
 		
 		//main labels
 		String[] labels = {"Program Outcome Category", "Program Outcome","Core Course Contributions","Service Course Contributions","Total Contribution" };
 		
 		/*index 0 = outcome group
 		 * 1 = program outcome
 	
 		
 		
 		
 		workbook.write();
 		workbook.close(); 
 		return file;
 	}
 
 	
 	
 	public static File createExcelFile(Program program) throws Exception
 	{
 		
 		ResourceBundle bundle = ResourceBundle.getBundle("currimap");
 		String folderName = bundle.getString("tempFileFolder");
 		File tempFolder = new File(folderName);
 		tempFolder = new File(tempFolder, ""+System.currentTimeMillis());
 		tempFolder.mkdirs();
 		Calendar cal = Calendar.getInstance();
 		SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM_dd_yyyy_H_mm");
 		File file = new File(tempFolder,program.getName() +"_"+ dateFormatter.format(cal.getTime())+".xls");
 		WritableWorkbook workbook = Workbook.createWorkbook(file); 
 	
 		WritableSheet sheet = workbook.createSheet("First Sheet", 0); 
 		
 		WritableFont biggerFont = new WritableFont(WritableFont.ARIAL, 14,WritableFont.BOLD, false);
 		
 		WritableCellFormat biggerFormat = new WritableCellFormat (biggerFont); 
 		biggerFormat.setWrap(true);
 		
 		WritableCellFormat wrappedCell = new WritableCellFormat();
 		wrappedCell.setWrap(true);
 		
 		int col = 0;
 		int row = 3;
 		Organization organization = program.getOrganization();
 		if(organization.getParentOrganization() != null)
 			organization = organization.getParentOrganization();
 		
 		List<CourseAttribute> courseAttributes = OrganizationManager.instance().getCourseAttributes(organization);
 		
 		
 		
 		//main labels
 		String[] labels = {"Program Outcome Category", "Program Outcome","Core Course Contributions","Service Course Contributions","Total Contribution" };
 		
 		/*index 0 = outcome group
 		 * 1 = program outcome
 		 * 2 = core courses
 		 * 3 = service courses
 		 * 4 = sum of contributions
 		 */
 		int[] mainColumns = {0,1,2,5,8};
 	
 		for(int i = 0; i< labels.length; i++)
 		{
 			Label labelToAdd = new Label(mainColumns[i], row, labels[i],biggerFormat);
 			sheet.addCell(labelToAdd);
 		}
 		//merge header main header cells
 		sheet.mergeCells(mainColumns[2], row, mainColumns[3]-1, row);
 		sheet.mergeCells(mainColumns[3], row, mainColumns[4]-1, row);
 			
 		for(int i = 0; i< Math.max(mainColumns[4]+1,(courseAttributes.size()+2)*2); i++)
 		{
 			sheet.setColumnView(i,  30);
 		}
 	
 		//add secondary headers
 		row++;
 		col = mainColumns[2];
 		Label courseHeaderLabel = new Label(col++, row, "Course",biggerFormat);
 		sheet.addCell(courseHeaderLabel);
 		
 		Label contributionsHeaderLabel = new Label(col, row, "Contribution-values",biggerFormat);
 		sheet.addCell(contributionsHeaderLabel);
 		sheet.mergeCells(col, row, col+4, row);
 		col += 5; //(leave 5 columns for contribution-values)
 		
 		col = mainColumns[3];
 		courseHeaderLabel = new Label(col++, row, "Course",biggerFormat);
 		sheet.addCell(courseHeaderLabel);
 		contributionsHeaderLabel = new Label(col, row, "Contribution-values",biggerFormat);
 		sheet.addCell(contributionsHeaderLabel);
 		sheet.mergeCells(col, row, col+4, row);
 		
 		
 		//start of program outcome-groups
 		row = 6; 
 		col = 0;
 		
 		ProgramManager pm = ProgramManager.instance();
 		Map<String, Integer> offeringCounts = pm.getCourseOfferingCounts(program );
 		List<LinkCourseProgram> courseLinks = pm.getLinkCourseProgramForProgram(program);
 
 		List<ProgramOutcomeGroup> groups = pm.getProgramOutcomeGroupsProgram(program);
 		
 		List<ProgramOutcomeCourseContribution> coreContributions = pm.getProgramOutcomeCoreCourseContributionForProgram(program);
 		List<ProgramOutcomeCourseContribution> serviceContributions = pm.getProgramOutcomeServiceCourseContributionForProgram(program);
 		NumberFormat formatter = NumberFormat.getInstance();
 		formatter.setMaximumFractionDigits(1);
 		for(ProgramOutcomeGroup group : groups)
 		{
 			
 			col = mainColumns[0];
 			List<LinkProgramProgramOutcome> programOutcomes = pm.getProgramOutcomeForGroupAndProgram(program,group);
 			
 			//program outcome group
 			Label groupLabel = new Label(col++, row, group.getName(),wrappedCell);
 			sheet.addCell(groupLabel);
 			
 			
 			for(LinkProgramProgramOutcome programOutcomeLink: programOutcomes)
 			{
 				int coreRow = row;
 				int serviceRow = row;
 				int programOutcomeTotalRow = row;
 				col = mainColumns[1];
 			
 				ProgramOutcome programOutcome = programOutcomeLink.getProgramOutcome();
 				//program outcome
 				Label outcomeLabel = new Label(col++, row, programOutcome.getName(),wrappedCell);
 				sheet.addCell(outcomeLabel);
 				
 				double total = 0.0;
 				
 				for(LinkCourseProgram courseLink :courseLinks)
 				{
 					double coreContribution = 0.0;
 					double serviceContribution = 0.0;
 					int coreColumn = mainColumns[2];
 					int serviceColumn = mainColumns[3];
 					
 					if(isContruibutingCourse(coreContributions, courseLink.getCourse(),programOutcome.getId()))
 					{
 						//put course info and attributes in starting at programCoreOutcomeColumn and programCoreOutcomeRow
 						placeCourseInfo(sheet,coreColumn++, coreRow, null, null, courseLink.getCourse());
 						
 						//put contributions in at programCoreOutcomeColumn + # of attributes and programCoreOutcomeRow
 						coreContribution = placeContributions(sheet,coreColumn, coreRow, coreContributions, programOutcome.getId() ,courseLink.getCourse().getId(),offeringCounts);
 						
 						//increase programCoreOutcomeRow
 						coreRow++;
 					}
 					else if  (isContruibutingCourse(serviceContributions, courseLink.getCourse(),programOutcome.getId()))
 						//this must be a service course
 					{
 						//put course info and attributes in starting at programServiceOutcomeColumn and programServiceOutcomeRow
 						placeCourseInfo(sheet,serviceColumn++, serviceRow, null, null, courseLink.getCourse());
 						
 						//put contributions in at programServiceOutcomeColumn + # of attributes and programServiceOutcomeRow
 						serviceContribution = placeContributions(sheet,serviceColumn, serviceRow, serviceContributions, programOutcome.getId() ,courseLink.getCourse().getId(),offeringCounts);
 						
 						//increase programServiceOutcomeRow
 						serviceRow++;
 					}
 					//next outcome row should be the max of programServiceOutcomeRow and programCoreOutcomeRow
 					total += coreContribution + serviceContribution;
 				}//done looping through courses for this program outcome
 				
 				row = Math.max(serviceRow, coreRow);
 				
 				Number totalNumber = new Number(mainColumns[4],programOutcomeTotalRow, total);
 				sheet.addCell(totalNumber);
 			}
 		
 		}
 		row++;
 		col = 0;
 		if(courseAttributes != null && !courseAttributes.isEmpty())
 		{
 			Label courseAttributesLabel = new Label(col, row++, "Course Attributes",biggerFormat);
 			sheet.addCell(courseAttributesLabel);
 			
 			int coreColumnHome = 0;
 			int serviceColumnHome = coreColumnHome + 2 + courseAttributes.size();
 			
 			int serviceColumn = serviceColumnHome;
 			int coreColumn = coreColumnHome;
 			
 			Label coreCourseAttributesLabel = new Label(coreColumn, row, "Core courses",biggerFormat);
 			sheet.addCell(coreCourseAttributesLabel);
 			
 			Label serviceCourseAttributesLabel = new Label(serviceColumn, row++, "Service courses",biggerFormat);
 			sheet.addCell(serviceCourseAttributesLabel);
 			
 			
 			Label courseNameHeaderLabel = new Label(coreColumn++, row, "Course",biggerFormat);
 			sheet.addCell(courseNameHeaderLabel);
 			courseNameHeaderLabel = new Label(serviceColumn++, row, "Course",biggerFormat);
 			sheet.addCell(courseNameHeaderLabel);
 			
 			for(CourseAttribute courseAttribute: courseAttributes)
 			{
 				Label labelToAdd = new Label(coreColumn++, row, courseAttribute.getName(),biggerFormat);
 				sheet.addCell(labelToAdd);
 				labelToAdd = new Label(serviceColumn++, row, courseAttribute.getName(),biggerFormat);
 				sheet.addCell(labelToAdd);
 			
 			}
 			row++;
 			col = 0;
 			int coreRow = row;
 			int serviceRow = row; 
 			
 			for(LinkCourseProgram courseLink :courseLinks)
 			{
 				serviceColumn = serviceColumnHome;
 				coreColumn = coreColumnHome;
 				List<Organization> orgs = CourseManager.instance().getOrganizationForCourse(courseLink.getCourse());
 				boolean orgMatches = false;
 				for(Organization org: orgs)
 				{
 					if(org.getId() == program.getOrganization().getId())
 						orgMatches = true;
 				}
 				List<CourseAttributeValue> attributeValues = CourseManager.instance().getCourseAttributeValues(courseLink.getCourse().getId(), program.getId());
 				
 				if(orgMatches) //core course
 				{
 					placeCourseInfo(sheet,coreColumn, coreRow++ , courseAttributes, attributeValues, courseLink.getCourse());
 						
 				}
 				else // service course
 				{
 					placeCourseInfo(sheet,serviceColumn, serviceRow++ , courseAttributes, attributeValues, courseLink.getCourse());
 				}
 			}
 		}
 		
 		
 		
 		
 		workbook.write();
 		workbook.close(); 
 		return file;
 	}
 	private static void placeCourseInfo(WritableSheet sheet,int column, int row, List<CourseAttribute> courseAttributes, List<CourseAttributeValue> attributeValues, Course course) throws RowsExceededException, WriteException
 	{
 		
 		Label courseContributionLabel = new Label(column++, row, course.getSubject() + " " + course.getCourseNumber());
 		sheet.addCell(courseContributionLabel);
 		if(courseAttributes != null)
 		{
 			for(CourseAttribute attribute : courseAttributes)
 			{
 				for(CourseAttributeValue value : attributeValues)
 				{
 					if(value.getAttribute().getId() == attribute.getId())
 					{	
 						Label courseAttrLabel = new Label(column++, row, value.getValue());
 						sheet.addCell(courseAttrLabel);
 					}
 				}
 			}
 		}
 	}
 	private static double placeContributions(WritableSheet sheet, int column, int row, List<ProgramOutcomeCourseContribution> contributions, int programOutcomeId ,int courseId,Map<String, Integer> offeringCounts) throws RowsExceededException, WriteException
 	{
 		double sum = 0.0;
 		double contributionValue = 0.0;
 		boolean contributionFound = false;
 		//do stuff for core course
 		for (ProgramOutcomeCourseContribution contribution:contributions)
 		{
 			
 			if(contribution.getCourseId() == courseId)
 			{
 				if(contribution.getProgramOutcomeId() == programOutcomeId)
 				{
 				
 					int count = offeringCounts.get(""+contribution.getCourseId()) != null?offeringCounts.get(""+contribution.getCourseId()): 1; 
 					contributionValue = (0.0+contribution.getContributionSum())/count;
 					sum = sum + contributionValue;
 					contributionFound = true;
 				}
 			}
 		}
 		if(contributionFound && contributionValue > 0.05)
 		{
 			Number number = new Number(column++, row, contributionValue);
 			sheet.addCell(number);
 		}
 		return sum;
 	}
 	
 	
 	private static boolean isContruibutingCourse(List<ProgramOutcomeCourseContribution> contributions, Course course, int programOutcomeId)
 	{
 		for (ProgramOutcomeCourseContribution contribution:contributions)
 		{
 			if(contribution.getCourseId() == course.getId() && contribution.getProgramOutcomeId() == programOutcomeId)
 			{
 				if(contribution.getContributionSum() > 0)
 					return true;
 			}
 		}
 		return false;
 	}
 
 
 }
