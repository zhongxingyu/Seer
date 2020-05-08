 package eu.trentorise.smartcampus.vas.ifame.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import jxl.Cell;
 import jxl.CellType;
 import jxl.Sheet;
 import jxl.Workbook;
 import jxl.WorkbookSettings;
 import jxl.read.biff.BiffException;
 import eu.trentorise.smartcampus.vas.ifame.model.MenuDelGiorno;
 import eu.trentorise.smartcampus.vas.ifame.model.MenuDelMese;
 import eu.trentorise.smartcampus.vas.ifame.model.MenuDellaSettimana;
 import eu.trentorise.smartcampus.vas.ifame.model.Piatto;
 
 /**
  * 
  * PRIMA DI FARE IL DEPLOY GUARDARE IL NUMERO DI FOGLI EXCEL E SETTARE LA
  * VARIABILE GLOBALE FOGLIO FINALE COME L ULTIMO FOGLIO CONTENENTE IL MENU DEL
  * PRANZO DI SOLITO SONO 5 MA PUÒ CAPITARE CHE LE SETTIMANE SIANO UN NUMERO
  * DIVERSO
  * 
  * DI SOLITO LE COLONNE E LE RIGHE SONO RISPETTATE MA E' SEMPRE MEGLIO DARGLI UN
  * OCCHIATA
  * 
  */
 public class NewMenuXlsUtil {
 
 	// ***************************************************************
 	// IMPORTANTE: DA CAMBIARE IN BASE AL NUMERO DI SETTIMANE DEL MESE
 	// IL FOGLIO FINALE
	// final int SHEET_FINALE = 5; // NOVEMBRE
	private static final int FOGLIO_FINALE = 6; // DICEMBRE
 	private static final int FOGLIO_INIZIALE = 0;
 	// ---------------------------------------------------------------
 	// RIGHE E COLONNE DEI MENU
 	private static final int COLONNA_INIZIALE = 0;
 	private static final int COLONNA_FINALE = 14;
 	private static final int RIGA_INIZIALE = 5;
 	private static final int RIGA_FINALE = 13;
 
 	// ***************************************************************
 
 	// le alternative sono uguali per tutte le settimane perciò leggiamo le
 	// alternative sul foglio 1 (COSÌ PER CONVENZIONE NOSTRA)
 	// NB: partono da 0 quindi sul secondo sheet
 	private static final int FOGLIO_ALTERNATIVE = 1;
 	// ---------------------------------------------------------------
 	// RIGHE E COLONNE DELLE ALTERNATIVE
 	private static final int ALTERNATIVE_COLONNA_INIZIALE = 0;
 	private static final int ALTERNATIVE_COLONNA_FINALE = 7;
 	private static final int ALTERNATIVE_RIGA_INIZIALE = 16;
 	private static final int ALTERNATIVE_RIGA_FINALE = 19;
 
 	// ***************************************************************
 
 	public static Workbook getWorkbook(InputStream stream)
 			throws BiffException, IOException {
 
 		WorkbookSettings xlsSettings = new WorkbookSettings();
 		xlsSettings.setDrawingsDisabled(true);
 
 		return Workbook.getWorkbook(stream, xlsSettings);
 	}
 
 	public static Workbook getWorkbook(String excelFileName)
 			throws BiffException, IOException {
 
 		File excelFile = new File(excelFileName);
 
 		WorkbookSettings xlsSettings = new WorkbookSettings();
 		xlsSettings.setDrawingsDisabled(true);
 
 		return Workbook.getWorkbook(excelFile, xlsSettings);
 	}
 
 	/**
 	 * Remove whitespace charachters at start and at the end upper case first
 	 * char and lowercase the rest
 	 */
 	public static String format(String name) {
 		name = name.trim();
 		return name.substring(0, 1).toUpperCase()
 				+ name.substring(1).toLowerCase();
 	}
 
 	/*
 	 * ALTERNATIVE
 	 */
 
 	public static List<Piatto> getAlternative(Workbook workbook) {
 
 		List<Piatto> listaPiatti = new ArrayList<Piatto>();
 
 		Sheet sheet = workbook.getSheet(FOGLIO_ALTERNATIVE);
 		for (int colonna = ALTERNATIVE_COLONNA_INIZIALE; colonna <= ALTERNATIVE_COLONNA_FINALE; colonna = colonna + 2) {
 			for (int riga = ALTERNATIVE_RIGA_INIZIALE; riga <= ALTERNATIVE_RIGA_FINALE; riga++) {
 				Cell nomePiatto = sheet.getCell(colonna, riga);
 				CellType type = nomePiatto.getType();
 				// check se la casella contiene qualcosa
 				if (type == CellType.LABEL) {
 					Cell kcal = sheet.getCell(colonna + 1, riga);
 					listaPiatti.add(new Piatto(
 							format(nomePiatto.getContents()), kcal
 									.getContents().trim()));
 				}
 			}
 		}
 
 		return listaPiatti;
 	}
 
 	/*
 	 * GET MENU DEL GIORNO
 	 */
 
 	public static MenuDelGiorno getMenuDelGiorno(Integer targetDay,
 			Workbook workbook) {
 
 		List<Piatto> listaPiatti = new ArrayList<Piatto>();
 		Integer currentDay = 0;
 
 		// ciclo sui fogli desiderati (evito la cena)
 		for (int numSettimana = FOGLIO_INIZIALE; numSettimana < FOGLIO_FINALE; numSettimana++) {
 			Sheet sheet = workbook.getSheet(numSettimana);
 
 			// controllo ogni due colonne (evitando le kcal)
 			for (int colonnaGiorno = COLONNA_INIZIALE; colonnaGiorno < COLONNA_FINALE; colonnaGiorno = colonnaGiorno + 2) {
 				Cell current_day = sheet.getCell(colonnaGiorno, RIGA_INIZIALE);
 				CellType day_type = current_day.getType(); // se
 
 				// il giorno esiste aumento il contatore
 				if (day_type == CellType.LABEL)
 					currentDay++;
 
 				// se il giorno ricercato è quello che corrente
 				if (currentDay == targetDay) {
 					for (int i = RIGA_INIZIALE; i < RIGA_FINALE; i++) {
 						Cell cell = sheet.getCell(colonnaGiorno, i);
 						CellType type = cell.getType();
 						if (type == CellType.LABEL) {
 							Cell kcal = sheet.getCell(colonnaGiorno + 1, i);
 							listaPiatti
 									.add(new Piatto(format(cell.getContents()),
 											kcal.getContents().trim()));
 						}
 					}
 				}
 			}
 		}
 
 		MenuDelGiorno mdg = new MenuDelGiorno();
 		mdg.setPiattiDelGiorno(listaPiatti);
 		mdg.setDay(targetDay);
 
 		return mdg;
 	}
 
 	/*
 	 * GET MENU DEL MESE
 	 */
 
 	public static MenuDelMese getMenuDelMese(Workbook workbook) {
 
 		// variabile per tenere il giorno corrente tracciato
 		int giornoDelMese = 0;
 		// creo il menu del mese e una lista di menu della settimana
 		MenuDelMese mdm = new MenuDelMese();
 		ArrayList<MenuDellaSettimana> mdslist = new ArrayList<MenuDellaSettimana>();
 		// ciclo sui fogli desiderati (evito la cena)
 		for (int numSettimana = FOGLIO_INIZIALE; numSettimana < FOGLIO_FINALE; numSettimana++) {
 			// ogni foglio ha una settimana
 			Sheet sheet = workbook.getSheet(numSettimana);
 			// variabile per tracciare qual'e' il primo giorno della settimana
 			// corrente
 			int giornoSettimanaCorrente = 0;
 			// ogni settimana ho un menu della settimana e una lista di menu del
 			// giorno
 			MenuDellaSettimana mds = new MenuDellaSettimana();
 			List<MenuDelGiorno> mdglist = new ArrayList<MenuDelGiorno>();
 			// controllo per ogni giorno due colonne (evitando le kcal)
 			for (int colonnaGiorno = COLONNA_INIZIALE; colonnaGiorno < COLONNA_FINALE; colonnaGiorno = colonnaGiorno + 2) {
 				Cell current_day = sheet.getCell(colonnaGiorno, RIGA_INIZIALE);
 				CellType day_type = current_day.getType();
 				// se ho scritte nella label il giorno esiste e aumento il
 				// contatore ho un giorno in piu
 				if (day_type == CellType.LABEL) {
 					// c'è il giorno incremento giorno corrente e giorno
 					// settimana corrente
 					giornoDelMese++;
 					giornoSettimanaCorrente++;
 					// controllo che non sia il primo della settimana altrimenti
 					if (giornoSettimanaCorrente == 1) {
 						// lo setto come startday
 						mds.setStart_day(giornoDelMese);
 					}
 					// ogni giorno ho un nuovo menu del giorno e una lista di
 					// piatti
 					MenuDelGiorno mdg = new MenuDelGiorno();
 					List<Piatto> piattiDelGiornoList = new ArrayList<Piatto>();
 					// ciclo sulle right del giorno considerato
 					for (int riga = RIGA_INIZIALE; riga < RIGA_FINALE; riga++) {
 
 						Cell piattoNameCell = sheet
 								.getCell(colonnaGiorno, riga);
 						CellType piattoNameCellType = piattoNameCell.getType();
 						// siccome dei giorni non hanno tutte le righe piene
 						// controllo
 						if (piattoNameCellType == CellType.LABEL) {
 							Cell kcalCell = sheet.getCell(colonnaGiorno + 1,
 									riga);
 
 							String value = piattoNameCell.getContents();
 
 							try {
 								value = new String(value.getBytes(), "UTF-8");
 							} catch (UnsupportedEncodingException e) {
 								e.printStackTrace();
 							}
 
 							piattiDelGiornoList.add(new Piatto(format(value),
 									kcalCell.getContents().trim()));
 						}
 					}
 					// setto il giorno corrente al menu del giorno
 					mdg.setDay(giornoDelMese);
 					// aggiungo la lista al menu del giorno
 					mdg.setPiattiDelGiorno(piattiDelGiornoList);
 					// aggiungo il menu del giorno alla lista di menu del giorno
 					// per la settimana corrente
 					mdglist.add(mdg);
 				}
 			}
 			// aggiungo la lista di menu del giorno al menu della settimana
 			mds.setMenuDelGiorno(mdglist);
 			// setto il giorno corrente come ultimo della settimana
 			mds.setEnd_day(giornoDelMese);
 			// aggiungo alla lista di menu della settimana il menu della
 			// settimana corrente
 			mdslist.add(mds);
 		}
 		// setto la lista di menu della settimana
 		mdm.setMenuDellaSettimana(mdslist);
 		// setto il primo e l'ultimo giorno del mese
 		mdm.setStart_day(1);
 		mdm.setEnd_day(giornoDelMese);
 
 		return mdm;
 	}
 }
