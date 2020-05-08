 package com.mutation.events.listener;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.util.GregorianCalendar;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 
 import com.mutation.report.om.ClassUnderTest;
 import com.mutation.report.om.Mutant;
 import com.mutation.report.om.MutationRatio;
 import com.mutation.report.om.MutationRun;
 import com.mutation.report.om.ProcessingInfo;
 import com.mutation.runner.events.IEvent;
 import com.mutation.runner.events.IEventListener;
 import com.mutation.runner.events.ProcessingClassUnderTest;
 import com.mutation.runner.events.ProcessingClassUnderTestFinished;
 import com.mutation.runner.events.ProcessingMutant;
 import com.mutation.runner.events.TestsExecuted;
 
 public class ReportEventListener implements IEventListener {
 
 	private MutationRun run;
 
 	private ClassUnderTest currentClassUnderTestSubReport;
 
 	private Mutant currentMutantReport;
 
 	private String outputFile;
 
 	private GregorianCalendar calendar;
 
 	private long classProcessingStart;
 
 	private long classProcessingEnd;
 
 	private long runProcessingStart;
 
 	private long runProcessingEnd;
 
 	private long numberOfMutantsForRun;
 
 	private long numberOfSurvivorsForRun;
 
 	private long numberOfMutantsForClass;
 
 	private long numberOfSurvivorsForClass;
 
 	public ReportEventListener(String fileName) throws JAXBException {
 
 		System.out.print("Initializing XML report: " + fileName);
 
 		run = new MutationRun();
 		this.outputFile = fileName;
 
 		calendar = new GregorianCalendar();
 		runProcessingStart = System.currentTimeMillis();
 
 	}
 
 	public void destroy() {
 
 		System.out.print("Finishing XML report");
 
 		runProcessingEnd = System.currentTimeMillis();
 
 		try {
 			ProcessingInfo info = new ProcessingInfo();
 			info.setDuration(runProcessingEnd - runProcessingStart);
 			calendar.setTimeInMillis(runProcessingStart);
 			info.setStart(DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar));
 			calendar.setTimeInMillis(runProcessingEnd);
 			info.setEnd(DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar));
 			run.setProcessingInfo(info);
 		} catch (DatatypeConfigurationException e) {
 			e.printStackTrace();
 		}
 
 		MutationRatio ratio = new MutationRatio();
 		ratio.setMutationCount(numberOfMutantsForRun);
 		ratio.setSurvivorCount(numberOfSurvivorsForRun);
 		run.setMutationRatio(ratio);
 
 		try {
 			JAXBContext context = JAXBContext.newInstance(MutationRun.class);
 			Marshaller marshaller = context.createMarshaller();
 			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 			marshaller.marshal(run, new FileOutputStream(new File(outputFile)));
 		} catch (JAXBException e) {
 			e.printStackTrace();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void notifyEvent(IEvent event) {
 
 //		System.out.println("Added event " + event.toString() + " to XML report");
 
 		if (event instanceof ProcessingClassUnderTest) {
 
 			classProcessingStart = System.currentTimeMillis();
 
 			ProcessingClassUnderTest eventObj = (ProcessingClassUnderTest) event;
 
 			currentClassUnderTestSubReport = new ClassUnderTest();
 			currentClassUnderTestSubReport.setName(eventObj.getClassUnderTest().getClassName());
 			currentClassUnderTestSubReport.setBaseClassFile(eventObj.getClassUnderTest().getClassFile());
			
			numberOfMutantsForClass = 0;
			numberOfSurvivorsForClass = 0;
 
 			run.getClassUnderTest().add(currentClassUnderTestSubReport);
 			return;
 
 		}
 
 		if (event instanceof ProcessingClassUnderTestFinished) {
 
 			ClassUnderTest classUnderTest = run.getClassUnderTest().get(run.getClassUnderTest().size() - 1);
 
 			classProcessingEnd = System.currentTimeMillis();
 			try {
 				ProcessingInfo info = new ProcessingInfo();
 				info.setDuration(classProcessingEnd - classProcessingStart);
 				calendar.setTimeInMillis(classProcessingStart);
 				info.setStart(DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar));
 				calendar.setTimeInMillis(classProcessingEnd);
 				info.setEnd(DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar));
 				classUnderTest.setProcessingInfo(info);
 			} catch (DatatypeConfigurationException e) {
 				e.printStackTrace();
 			}
 
 			MutationRatio ratio = new MutationRatio();
 
 			ratio.setMutationCount(numberOfMutantsForClass);
 			ratio.setSurvivorCount(numberOfSurvivorsForClass);
 
 			classUnderTest.setMutationRatio(ratio);
 
 			return;
 
 		}
 
 		if (event instanceof ProcessingMutant) {
 
 			ProcessingMutant eventObj = (ProcessingMutant) event;
 
 			Mutant mutant = new Mutant();
 			mutant.setName(eventObj.getMutant().getName());
 			mutant.setBaseSourceLine(eventObj.getMutant().getSourceMapping().getLineNo());
 			mutant.setOperatorName(eventObj.getMutant().getMutationType().name());
 
 			currentMutantReport = mutant;
 
 			currentClassUnderTestSubReport.getMutant().add(currentMutantReport);
 			currentClassUnderTestSubReport.setBaseSourceFile(eventObj.getMutant().getSourceMapping().getSourceFile());
 			return;
 
 		}
 
 		if (event instanceof TestsExecuted) {
 
 			TestsExecuted eventObj = (TestsExecuted) event;
 
 			currentMutantReport.setSurvived(eventObj.isMutantSurvived());
 
 			numberOfMutantsForClass++;
 			numberOfMutantsForRun++;
 
 			if (eventObj.isMutantSurvived()) {
 				numberOfSurvivorsForClass++;
 				numberOfSurvivorsForRun++;
 			}
 
 			return;
 
 		}
 
 	}
 }
