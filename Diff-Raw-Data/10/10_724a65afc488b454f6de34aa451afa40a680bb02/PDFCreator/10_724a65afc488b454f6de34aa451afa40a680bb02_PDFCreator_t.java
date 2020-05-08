 package de.atlassoft.io.docexport;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 
 
 import com.itextpdf.text.BadElementException;
 import com.itextpdf.text.Chunk;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.Image;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.Phrase;
 import com.itextpdf.text.Section;
 import com.itextpdf.text.pdf.PdfPCell;
 import com.itextpdf.text.pdf.PdfPTable;
 import de.atlassoft.model.Node;
 import de.atlassoft.model.ScheduleScheme;
 import de.atlassoft.model.ScheduleType;
 import de.atlassoft.model.SimulationStatistic;
 import de.atlassoft.model.TrainRideStatistic;
 
 //TODO: implementieren, Konstruktor fehlt noch.
 /**
  * Is used to generate a PDF document.
  * 
  * @author Andreas Szlatki & Linus Neler
  * 
  */
 class PDFCreator {
 
 	/**
 	 * One of the Fonts used in the PDF document.
 	 */
 	private Font bigBold = new Font(Font.FontFamily.TIMES_ROMAN, 24, Font.BOLD);
 
 	/**
 	 * One of the Fonts used in the PDF document.
 	 */
 	private Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 18,
 			Font.BOLD);
 
 	/**
 	 * Generates empty lines in an specific paragraph. If paragraph is null, an
 	 * {@link IllegalArgumentException} is thrown.
 	 * 
 	 * @param paragraph
 	 *            {@link Paragraph} which gets edited.
 	 * @param number
 	 *            The number of empty lines which should be added.
 	 * 
 	 */
 	protected void addEmptyLine(Paragraph paragraph, int number) {
 		if (paragraph == null) {
 			throw new IllegalArgumentException("paragraph must not be null");
 		}
 		for (int i = 0; i < number; i++) {
 			paragraph.add(new Paragraph(" "));
 		}
 	}
 
 	/**
 	 * Generates Meta data to a PDF document.Accessiable in documents
 	 * property's. If document is null, an {@link IllegalArgumentException} is
 	 * thrown.
 	 * 
 	 * @param document
 	 *            {@link Document} to which the Metadata should be added.
 	 * 
 	 */
 	protected void addMetaData(Document document, String title) {
 		if (document == null) {
 			throw new IllegalArgumentException("document must not be null");
 		}
 		document.addTitle(title);
 		document.addSubject("Using iText");
 		document.addKeywords("Java, PDF, iText");
 		document.addAuthor("ATLASsoft");
 		document.addCreator("ATLASsoft");
 	}
 
 	/**
 	 * Generates a title page for a PDF document. If document is null, an
 	 * {@link IllegalArgumentException} is thrown. If the insertion to the
 	 * document fails an {@link DocumentException} is thrown.
 	 * 
 	 * @param document
 	 *            {@link Document} which gets edited.
 	 * @param title
 	 *            The name of the title.
 	 * @param description
 	 *            The description to the title page.
 	 * @throws DocumentException
 	 * @throws IOException
 	 * @throws MalformedURLException
 	 * 
 	 */
 	protected void addTitlePage(Document document, String title,
 			String description, String identifier) throws DocumentException,
 			MalformedURLException, IOException {
 		if (document == null) {
 			throw new IllegalArgumentException("document must not be null");
 		}
 		Paragraph preface = new Paragraph();
 		// We add one empty line
 		addEmptyLine(preface, 1);
 
 		Chunk underlineUeberschrift = new Chunk(title, bigBold);
 		underlineUeberschrift.setUnderline(0.1f, -2f);
 
 		Paragraph ueberschriftr = new Paragraph(underlineUeberschrift);
 		ueberschriftr.setAlignment(Element.ALIGN_CENTER);
 
 		// Lets write a big header
 		preface.add(ueberschriftr);
 
 		// Description
 		addEmptyLine(preface, 10);
 		preface.add(new Paragraph(description, bigBold));
 		Paragraph id = new Paragraph(identifier, bigBold);
 		id.setAlignment(Element.ALIGN_CENTER);
 		preface.add(new Paragraph(id));
 
 		addEmptyLine(preface, 8);
 
 		Image imgToAdd = Image.getInstance("img/ATLASsoftLogo.gif");
 		imgToAdd.setAlignment(Element.ALIGN_CENTER);
 		document.add(preface);
 		document.add(imgToAdd);
 		// Start a new page
 		document.newPage();
 	}
 
 	protected String getWeekDay(int dayOfWeek) {
 		String res = null;
 		switch (dayOfWeek) {
 		case 1:
 			res = "Montag";
 			break;
 		case 2:
 			res = "Dienstag";
 			break;
 		case 3:
 			res = "Mittwoch";
 			break;
 		case 4:
 			res = "Donnerstag";
 			break;
 		case 5:
 			res = "Freitag";
 			break;
 		case 6:
 			res = "Samstag";
 			break;
 		case 7:
 			res = "Sonntag";
 			break;
 		default:
 			res = "Invalid Day";
 			break;
 		}
 
 		return res;
 	}
 
 	/**
 	 * Adds content to the document. If document is null, an
 	 * {@link IllegalArgumentException} is thrown. If the insertion to the
 	 * document fails an {@link DocumentException} is thrown.
 	 * 
 	 * @param document
 	 *            {@link Document} which gets edited.
 	 * @param data
 	 *            The actual content which should be added.
 	 * @throws DocumentException
 	 * 
 	 */
 	protected void addScheduleContent(Document document, ScheduleScheme schedule)
 			throws DocumentException {
 		if (document == null) {
 			throw new IllegalArgumentException("document must not be null");
 		}
 		String fahrplanTyp;
 		DateFormat dateFormat = new SimpleDateFormat("HH:mm");
 		// Einzelfahrt
 		if (schedule.getScheduleType() == ScheduleType.SINGLE_RIDE) {
 			fahrplanTyp = "Einzelfahrt";
 
 			// Fharplanname
 			Paragraph scheduleName = new Paragraph("Fahrplanname :  "
 					+ schedule.getID(), smallBold);
 			// Fahrplantyp
 			Paragraph scheduleType = new Paragraph("Fahrplantyp :  "
 					+ fahrplanTyp, smallBold);
 			// Fahrtage
 			Paragraph drivingDays = new Paragraph("Fhrt an den Tagen :  ",
 					smallBold);
 			List<Integer> dday = schedule.getDays();
 			for (int i = 0; i < dday.size(); i++) {
 				if (i == dday.size() - 1) {
 					drivingDays.add(getWeekDay(dday.get(i)));
 				} else {
 					drivingDays.add(getWeekDay(dday.get(i)) + ", ");
 				}
 			}
 			// Abfahrtszeit der Einzelfahrten
 			Paragraph abfahrtszeit = new Paragraph("Abfahrtszeit :  "
 					+ dateFormat.format(schedule.getFirstRide().getTime()),
 					smallBold);
 			// Streckennetz
 			Paragraph railsys = new Paragraph("Gehrt zu Streckennetz :  "
 					+ schedule.getRailSysID(), smallBold);
 
 			addEmptyLine(railsys, 3);
 			addEmptyLine(abfahrtszeit, 1);
 			addEmptyLine(scheduleName, 1);
 			addEmptyLine(scheduleType, 1);
 			addEmptyLine(drivingDays, 1);
 			document.add(scheduleName);
 			document.add(scheduleType);
 			document.add(drivingDays);
 			document.add(abfahrtszeit);
 			document.add(railsys);
 
 		} // Intervallfahrt
 		else {
 
 			fahrplanTyp = "Intervallfahrt";
 			// Fharplanname
 			Paragraph scheduleName = new Paragraph("Fahrplanname :  "
 					+ schedule.getID(), smallBold);
 			// Fahrplantyp
 			Paragraph scheduleType = new Paragraph("Fahrplantyp :  "
 					+ fahrplanTyp, smallBold);
 			// Erster Fahrtag
 			Paragraph firstDay = new Paragraph("Erste Fahrt :  "
 					+ dateFormat.format(schedule.getFirstRide().getTime()),
 					smallBold);
 			// Letzter Fahrtag
 			Paragraph lastDay = new Paragraph("Letzte Fahrt :  "
 					+ dateFormat.format(schedule.getLastRide().getTime()),
 					smallBold);
 			// Fahrtage
 			Paragraph drivingDays = new Paragraph("Fhrt an den Tagen :  ",
 					smallBold);
 			List<Integer> dday = schedule.getDays();
 			for (int i = 0; i < dday.size(); i++) {
 				if (i == dday.size() - 1) {
 					drivingDays.add(getWeekDay(dday.get(i)));
 				} else {
 					drivingDays.add(getWeekDay(dday.get(i)) + ", ");
 				}
 			}
 			// Intervall
 			Paragraph intervall = new Paragraph("Fhrt alle :  "
 					+ schedule.getInterval() + " Minuten", smallBold);
 			// Streckennetz
 			Paragraph railsys = new Paragraph("Gehrt zu Streckennetz :  "
 					+ schedule.getRailSysID(), smallBold);
 			addEmptyLine(railsys, 3);
 			addEmptyLine(scheduleName, 1);
 			addEmptyLine(scheduleType, 1);
 			addEmptyLine(firstDay, 1);
 			addEmptyLine(lastDay, 1);
 			addEmptyLine(drivingDays, 1);
 			addEmptyLine(intervall, 1);
 			document.add(scheduleName);
 			document.add(scheduleType);
 			document.add(firstDay);
 			document.add(lastDay);
 			document.add(drivingDays);
 			document.add(intervall);
 			document.add(railsys);
 		}
 
 		// Angefahrene Haltestellen Tabelle
 		Paragraph table = new Paragraph("Angefahrene Haltestellen:", smallBold);
 		addEmptyLine(table, 2);
 		createTable(table, schedule);
 		document.add(table);
 		System.out.println("Schedule data saved!");
 	}
 
 	/**
 	 * Adds content to the document. If document is null, an
 	 * {@link IllegalArgumentException} is thrown. If the insertion to the
 	 * document fails an {@link DocumentException} is thrown.
 	 * 
 	 * @param document
 	 *            {@link Document} which gets edited.
 	 * @param data
 	 *            The actual content which should be added.
 	 * @throws DocumentException
 	 * 
 	 */
 	protected void addStatisticContent(Document document,
 			SimulationStatistic stat) throws DocumentException {
 		if (document == null) {
 			throw new IllegalArgumentException("document must not be null");
 		}
		if (stat == null) {
			throw new IllegalArgumentException("stat must not be null");
		}
 		DecimalFormat doubleFormater = new DecimalFormat("#0.00");
 
 		List<TrainRideStatistic> tRS = stat.getStatistics();
 		Double trainTypeWithMostMDelay = 0.0;
 		Double scheduleSchemeMostMDelay = 0.0;
 		Double nodeMostMDelay = 0.0;
 		String nodeMMD = "";
 		String scheduleMMD = "";
 		String trainTypeMMD = "";
 		int scheduleNumberOfRides = 0;
 		int trainTypeNumberOfRides = 0;
 		List<ScheduleScheme> allScheduleIDS = stat.getInvolvedScheduleSchemes();
 		String railSysID = stat.getRailwaySystem().getID();
 		
 		for (TrainRideStatistic trs : tRS) {
 			// Liste ber all Knoten der Statistik + Knoten mit most MeanDelay
 			List<Node> nodeStat = trs.getStations();
 			for (Node node : nodeStat) {
 				if (stat.getMeanDelay(node) > nodeMostMDelay) {
 					nodeMostMDelay = stat.getMeanDelay(node);
 					nodeMMD = node.getName();
 				}
 			}
 		
 			// ScheduleScheme with most MeanDelay
 			if (stat.getMeanDelay(trs.getScheduleScheme()) > scheduleSchemeMostMDelay) {
 				scheduleSchemeMostMDelay = stat.getMeanDelay(trs
 						.getScheduleScheme());
 				// + Anzahl der Fahrten auf diesem ScheduleScheme
 				scheduleNumberOfRides = stat.getNumberOfRides(trs
 						.getScheduleScheme());
 				scheduleMMD = trs.getScheduleScheme().getID();
 			}
 
 			// TrainType with most MeanDelay!
 			if (stat.getMeanDelay(trs.getScheduleScheme().getTrainType()) > trainTypeWithMostMDelay) {
 				trainTypeWithMostMDelay = stat.getMeanDelay(trs
 						.getScheduleScheme().getTrainType());
 				// + Anzahl der Fahrten mit diesem Zugtyp
 				trainTypeNumberOfRides = stat.getNumberOfRides(trs
 						.getScheduleScheme().getTrainType());
 				trainTypeMMD = trs.getScheduleScheme().getTrainType().getName();
 			}
 
 		}
 		// General Data
 		Paragraph involvedRailSysInf = new Paragraph ("Aktives Streckennetz: ", smallBold);
 		addEmptyLine(involvedRailSysInf, 1);
 		document.add(involvedRailSysInf);
 		Paragraph invovedRailSys = new Paragraph(railSysID);
 		addEmptyLine(invovedRailSys, 1);
 		document.add(invovedRailSys);
 		Paragraph involvedSchedulesInf = new Paragraph ("Aktive Fahrplne: ", smallBold);
 		addEmptyLine(involvedSchedulesInf, 1);
 		document.add(involvedSchedulesInf);
 		Paragraph invovedSchedules = new Paragraph("");
 		for (ScheduleScheme scheduleScheme : allScheduleIDS) {
 			if (scheduleScheme
 					== (allScheduleIDS.get(allScheduleIDS.size() - 1))) {
 				invovedSchedules.add(scheduleScheme.getID());
 			} else{
 			invovedSchedules.add(scheduleScheme.getID() + ", ");
 			}
 		}
 		addEmptyLine(invovedSchedules, 1);
 		document.add(invovedSchedules);
 		Paragraph contentNumbers = new Paragraph(
 				"Gesamte Anzahl der Fahrten, innerhalb der Simulation: ",
 				smallBold);
 		addEmptyLine(contentNumbers, 1);
 		document.add(contentNumbers);
 		Paragraph numberOfRides = new Paragraph(Integer.toString(stat.getNumberOfRides()) + "  Fahrt/en");
 		addEmptyLine(numberOfRides, 1);
 		document.add(numberOfRides);
 		Paragraph contentMMD = new Paragraph("Durchschnittliche Versptung, innerhalb der Simulation: ", smallBold);
 		addEmptyLine(contentMMD, 1);
 		document.add(contentMMD);
 		Paragraph MMD = new Paragraph(doubleFormater.format(stat.getMeanDelay() / 60.0)
 				+ "  Minute/n");
 		addEmptyLine(MMD, 1);
 		document.add(MMD);
 		Paragraph contentDelay = new Paragraph("Gesamte Versptung der Simulation: ", smallBold);
 		addEmptyLine(contentDelay, 1);
 		document.add(contentDelay);
 		Paragraph delay = new Paragraph(doubleFormater.format(stat.getTotalDelay() / 60.0)
 				+ "  Minute/n");
 		addEmptyLine(delay, 2);
 		document.add(delay);
 		// Significant Statistics
 		Paragraph eskalation = new Paragraph("Eskalationen:", smallBold);
 		eskalation.setAlignment(Element.ALIGN_CENTER);
 		addEmptyLine(eskalation, 1);
 		document.add(eskalation);
 		// Node Data
 		Paragraph nodeData = new Paragraph(
 				"Station mit der hchsten, durchschnittlichen Versptung: ",
 				smallBold);
 		addEmptyLine(nodeData, 1);
 		document.add(nodeData);
 		Chunk underlineNode = new Chunk(nodeMMD);
 		underlineNode.setUnderline(0.1f, -2f);
 		Paragraph nodeName = new Paragraph(underlineNode);
 		addEmptyLine(nodeName, 1);
 		document.add(nodeName);
 		Paragraph nodedelay = new Paragraph(
 				doubleFormater.format(nodeMostMDelay / 60.0)
 						+ "  Minute/n Versptung");
 		addEmptyLine(nodedelay, 1);
 		document.add(nodedelay);
 		// Schedule Data
 		Paragraph scheduleData = new Paragraph(
 				"Fahrplan mit der hchsten, durchschnittlichen Versptung:  ",
 				smallBold);
 		addEmptyLine(scheduleData, 1);
 		document.add(scheduleData);
 		Chunk underlineSchedule = new Chunk(scheduleMMD);
 		underlineSchedule.setUnderline(0.1f, -2f);
 		Paragraph scheduleName = new Paragraph(underlineSchedule);
 		addEmptyLine(scheduleName, 1);
 		document.add(scheduleName);
 		Paragraph scheduleDelay = new Paragraph(
 				doubleFormater.format(scheduleSchemeMostMDelay / 60.0)
 						+ "  Minute/n Versptung");
 		addEmptyLine(scheduleDelay, 1);
 		scheduleDelay.add("Anzahl der Fahrten innerhalb des Fahrplans:  "
 				+ scheduleNumberOfRides);
 		document.add(scheduleDelay);
 		// Traintype Data
 		Paragraph trainTypeData = new Paragraph(
 				"Zugtyp mit der hchsten, durchschnittlichen Versptung: ",
 				smallBold);
 		addEmptyLine(trainTypeData, 1);
 		document.add(trainTypeData);
 		Chunk underlineTrainType = new Chunk(trainTypeMMD);
 		underlineTrainType.setUnderline(0.1f, -2f);
 		Paragraph trainTypeName = new Paragraph(underlineTrainType);
 		addEmptyLine(trainTypeName, 1);
 		document.add(trainTypeName);
 		Paragraph trainTypeDelay = new Paragraph(
 				doubleFormater.format(trainTypeWithMostMDelay / 60.0)
 						+ " Minute/n Versptung");
 		addEmptyLine(trainTypeDelay, 1);
 		trainTypeDelay.add("Anzahl der Fahrten des Zugtyps: "
 				+ trainTypeNumberOfRides);
 		document.add(trainTypeDelay);
 		
 		System.out.println("Statistic saved!");
 	}
 
 	/**
 	 * Adds content to the document. If document is null, an
 	 * {@link IllegalArgumentException} is thrown. If the insertion to the
 	 * document fails an {@link DocumentException} is thrown.
 	 * 
 	 * @param document
 	 *            {@link Document} which gets edited.
 	 * @param data
 	 *            The actual content which should be added.
 	 * @throws DocumentException
 	 * 
 	 */
 	protected void addDepartureContent(Document document, Node station,
 			List<ScheduleScheme> scedList) throws DocumentException {
 		if (document == null) {
 			throw new IllegalArgumentException("document must not be null");
 		}
		if (station == null) {
			throw new IllegalArgumentException("station must not be null");
		}
		if (scedList == null) {
			throw new IllegalArgumentException("scedList must not be null");
		}
 		List<Integer> relevantSpots = new ArrayList<Integer>();
 		for (ScheduleScheme sced : scedList) {
 //TODO comments entfernen!
 			int remove = 0;
 			List<Node> nodeList = sced.getStations();
 			//System.out.println(nodeList.size());
 			for (int i = 0; i < nodeList.size(); i++) {
 				if (station.getName().equals(nodeList.get(i).getName())) {
 					relevantSpots.add(i);
 					remove = i;
 				}
 			}
 			//System.out.println(relevantSpots.size());
 			for (int relevantSpot : relevantSpots) {
 				int result = sced.getArrivalTimes().get(relevantSpot)
 						+ sced.getIdleTimes().get(relevantSpot);
 				//System.out.println(result);
 				Calendar newCal;
 				newCal = sced.getFirstRide();
 				newCal.add(Calendar.SECOND, result);
 				DateFormat arrivalFormat = new SimpleDateFormat("HH:mm");
 				Paragraph name = new Paragraph(station.getName(), smallBold);
 				for (int y = relevantSpot + 1; y < nodeList.size(); y++) {
 					name.add(" - " + nodeList.get(y).getName());
 				}
 				Paragraph zeit = new Paragraph(arrivalFormat.format(newCal
 						.getTime()));
 
 				String scedType;
 				if (sced.getScheduleType() == ScheduleType.SINGLE_RIDE) {
 					scedType = "Einzelfahrt";
 				} else {
 					scedType = "Intervallfahrt, fhrt alle: "
 							+ sced.getInterval() + " Minuten";
 				}
 				zeit.add("     " + scedType + " mit "
 						+ sced.getTrainType().getName());
 				document.add(name);
 				document.add(zeit);
 			}
 			relevantSpots.remove(remove);
 		}
 		System.out.println("Departureboard saved!");
 	}
 
 	/**
 	 * Generates a table in the PDF document.If subCatPart is null, an
 	 * {@link IllegalArgumentException} is thrown. If an Element gets added
 	 * which doesn't fit the tables property's, an {@link BadElementException}
 	 * is thrown.
 	 * 
 	 * @param subCatPart
 	 *            {@link Section} describes the structure and the content of the
 	 *            table.
 	 * 
 	 */
 	protected void createTable(Paragraph buildTable, ScheduleScheme sced) {
 		if (buildTable == null) {
 			throw new IllegalArgumentException("document must not be null");
 		}
 		PdfPTable table = new PdfPTable(3);
 
 		PdfPCell c1 = new PdfPCell(new Phrase("Name"));
 		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
 		table.addCell(c1);
 
 		if (sced.getScheduleType() == ScheduleType.SINGLE_RIDE) {
 			c1 = new PdfPCell(new Phrase("Ankunftszeit"));
 		} else {
 			c1 = new PdfPCell(new Phrase("Ankunft nach (in Minuten)"));
 		}
 
 		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
 		table.addCell(c1);
 
 		c1 = new PdfPCell(new Phrase("Aufenthaltszeit (in Minuten)"));
 		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
 		table.addCell(c1);
 		table.setHeaderRows(1);
 
 		List<Integer> arrivalTimesList = sced.getArrivalTimes();
 		List<Integer> idleTimesList = sced.getIdleTimes();
 		List<Node> nodeList = sced.getStations();
 
 		DecimalFormat doubleFormater = new DecimalFormat("#0");
 		DateFormat arrivalFormat = new SimpleDateFormat("HH:mm");
 		double idleMin;
 		double arrivalMin;
 		Calendar newCal;
 
 		for (int i = 0; i < nodeList.size(); i++) {
 
 			// Adding the seconds of the i-th arrival time to the start time
 			newCal = sced.getFirstRide();
 			newCal.add(Calendar.SECOND, arrivalTimesList.get(i));
 
 			// Calculating the idle time in minutes
 			idleMin = idleTimesList.get(i) / 60.0;
 			arrivalMin = arrivalTimesList.get(i) / 60.0;
 
 			// adding the Content to the table
 			table.addCell(nodeList.get(i).getName());
 
 			if (sced.getScheduleType() == ScheduleType.SINGLE_RIDE) {
 				table.addCell(arrivalFormat.format(newCal.getTime()));
 			} else {
 				table.addCell(doubleFormater.format(arrivalMin));
 			}
 
 			table.addCell(doubleFormater.format(idleMin));
 		}
 
 		buildTable.add(table);
 	}
 
 }
