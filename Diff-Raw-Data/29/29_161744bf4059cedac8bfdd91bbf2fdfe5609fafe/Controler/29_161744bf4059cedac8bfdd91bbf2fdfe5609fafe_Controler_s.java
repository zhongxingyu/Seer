 package com.rocel.fabapp.controler;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.rocel.fabapp.model.BDManager;
 import com.rocel.fabapp.model.Bateau;
 import com.rocel.fabapp.model.Categorie;
 import com.rocel.fabapp.model.DegatRep;
 import com.rocel.fabapp.model.JeuxAvirons;
 import com.rocel.fabapp.model.Rameur;
 import com.rocel.fabapp.model.TypeRameur;
 import com.rocel.fabapp.model.ZoneSorties;
 import com.rocel.fabapp.vue.MainVue;
 import com.rocel.fabapp.vue.display.DisplayBateaux;
 import com.rocel.fabapp.vue.display.DisplayRameurs;
 
 public class Controler implements IControler{
 	private BDManager dbm;
 	public static SimpleDateFormat formatterDate = new SimpleDateFormat("dd/MM/yyyy");
 	public static SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm");
 	
 	public Controler() {
 		this.dbm = BDManager.getInstance();
 	}
 
 	public void quit(){
 		System.exit(0);
 	}
 
 	/**
 	 * Check email with regexp
 	 * @param email to check
 	 * @return boolean: True if the parameter is an email, else, False
 	 */
 	@Override
 	public boolean checkEmail(String email) {
 		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
 		Matcher m = p.matcher(email);
 		return m.matches();
 	}
 
 
 	@Override
 	public boolean checkNumber(String number) {
 		Pattern p = Pattern.compile("([0-9]*.?[0-9]*)");
 		Matcher m = p.matcher(number);
 		return m.matches();
 	}
 	
 	@Override
 	public boolean checkLicence(String liccence) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	@Override
 	public boolean checkCoulisse(String coulisse) {
 		Pattern p = Pattern.compile("\\d+([,.]\\d*)?");
 		Matcher m = p.matcher(coulisse);
 		return m.matches();
 	}
 
 	@Override
 	public boolean checkTelephone(String telephone) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean saveRameur(String nom, String prenom, Date dateNaissance, String categorie, String type, String adresse, String email,
 								String telephone, String morphologie, int pointure, String licence) {
 		Rameur rameur = new Rameur(nom, prenom, dateNaissance, Categorie.valueOf(categorie), TypeRameur.valueOf(type), adresse, email, telephone, morphologie, pointure, licence);
 		return this.dbm.saveRameur(rameur);
 	}
 
 	@Override
 	public boolean saveDegatRep(Date dateDegat, String observation,
 			String zoneDegat, String refRameur, int refBateau,
 			String importanceDegat, String typeReparation, double cout,
 			String reparePar){
 		Rameur rameur = this.dbm.getRameurByNBLicence(refRameur);
 		Bateau bateau = this.dbm.getBateauByRef(refBateau);
 		DegatRep degatRep = new DegatRep(dateDegat, observation, zoneDegat, rameur, bateau, importanceDegat, typeReparation, cout, reparePar);
 		return this.dbm.saveDegatRep(degatRep);
 	}
 	
 	@Override
 	public boolean saveBateau(String type, int portance, int coulisse, String portant, String materiaux, String marque, String modele,
 							Date dateFabrication, Date dateAcquisition, int garantie, String equipage) {
 		List<Rameur> equi = new ArrayList<Rameur>();
 		Bateau bateau = new Bateau(type, portance, coulisse, portant, materiaux, marque, modele, dateFabrication, dateAcquisition, garantie, equi);
 		return this.dbm.saveBateau(bateau);
 	}
 
 	@Override
 	public boolean saveJeuxAvirons(int quantite, boolean reglable, String marque,
 			String modele, Date dateFabrication, Date dateAcquisition,
 			int garantie, int refBateau){
 		Bateau bateau = this.dbm.getBateauByRef(refBateau);
 		JeuxAvirons jeuxAviron = new JeuxAvirons(quantite, reglable, marque, modele, dateFabrication, dateAcquisition, garantie, bateau);
 		return this.dbm.saveJeuxAvirons(jeuxAviron);
 	}
 	
 	@Override
 	public String[] getBateauxTypes(){
 		return Bateau.typesBateau;
 	}
 
 	@Override
 	public String[] getBateauxTypesSearch(){
 		String[] types = new String[Bateau.typesBateau.length + 1];
 		int i = 0;
 		types[i] = "";
 		for(int j = 0; j < Bateau.typesBateau.length;j++){
 			types[++i] = Bateau.typesBateau[j];
 		}
 		return types;
 	}
 
 	@Override
 	public Date stringToDate(String sDate){
 		Date date = null;
 		try{
 			date = formatterDate.parse(sDate);
 		}catch(Exception e){
 			e.printStackTrace();
 		}
         return date;
     }
 
 	@Override
 	public Date stringToTime(String sDate){
 		Date date = null;
 		try{
 			date = formatterTime.parse(sDate);
 		}catch(Exception e){
 			e.printStackTrace();
 		}
         return date;
     }
 
 	@Override
 	public void searchRameurs(MainVue parent, String nom, String prenom, Date dateNaissance,
 			String categorieIN, String typeIN, String adresse, String email,
 			String telephone, String morphologie, int pointure,
 			String licence) {
 		Categorie categorie = null;
 		TypeRameur type = null;
 		if(!categorieIN.isEmpty()){
 			categorie = Categorie.valueOf(categorieIN);
 		}
 		if(!typeIN.isEmpty()){
 			type = TypeRameur.valueOf(typeIN);
 		}
 		List<Rameur> liste = this.dbm.getRameurs(nom, prenom, dateNaissance, categorie, type, adresse, email, telephone, morphologie, pointure, licence);
 		Object[][] data = new Object[liste.size()][11];
 		for (int j =0;j<liste.size();j++) {
 			data[j][0] = liste.get(j).getNom();
 			data[j][1] = liste.get(j).getPrenom();
 			data[j][2] = formatterDate.format(liste.get(j).getDateNaissance());
 			data[j][3] = liste.get(j).getCategorie().toString();
 			data[j][4] = liste.get(j).getType().toString();
 			data[j][5] = liste.get(j).getAdresse();
 			data[j][6] = liste.get(j).getEmail();
 			data[j][7] = liste.get(j).getTelephone();
 			data[j][8] = liste.get(j).getMorphologie();
 			data[j][9] = liste.get(j).getPointure();
 			data[j][10] = liste.get(j).getLicence();
 		}
 		DisplayRameurs panel = DisplayRameurs.getInstance();
 		parent.displayPanel(panel);
 		panel.setData(data);
 	}
 
 	@Override
 	public void searchBateaux(MainVue parent, String type, int portance, int coulisse, String portant, String materiaux, String marque, String modele,
 			Date dateFabrication, Date dateAcquisition, int garantie, String equipage) {
 		List<Bateau> liste = this.dbm.getBateaux(type, portance, coulisse, portant, materiaux, marque, modele, dateFabrication, dateAcquisition, garantie, getEquipage(equipage));		
 		Object[][] data = new Object[liste.size()][12];
 		for (int j = 0; j < liste.size() ; j++) {
 			data[j][0] = liste.get(j).getRef();
 			data[j][1] = liste.get(j).getMarque();
 			data[j][2] = liste.get(j).getModele();
 			data[j][3] = liste.get(j).getType();
 			data[j][4] = liste.get(j).getPortant();
 			data[j][5] = liste.get(j).getPortance();
 			data[j][6] = liste.get(j).getCoulisse();
 			data[j][7] = liste.get(j).getMateriaux();
 			data[j][8] = formatterDate.format(liste.get(j).getDateFabrication());
 			data[j][9] = formatterDate.format(liste.get(j).getDateAcquisition());
 			data[j][10] = liste.get(j).getGarantie();
 			data[j][11] = getEquipage(liste.get(j).getEquipage());
 		}
 		DisplayBateaux panel = DisplayBateaux.getInstance();
 		parent.displayPanel(panel);
 		panel.setData(data);
 	}
 
 	/**.
 	 * Get String list (format de la bdd) de rameurs formant un equipage
 	 * @param list de Rameurs
 	 * @return equipage au format "nbLicence/nblicence/..."
 	 */
 	public String getEquipage(List<Rameur> equipage) {
 		String equi = "";
 		if (equipage != null){
 			for (Rameur r: equipage) {
 				equi += r.getLicence() +"/";
 			}
 		}
 		return equi;
 	}
 
 	/**
 	 * Get list de rameur formant un equipage
 	 * @param equipage au format "nbLicence/nblicence/..."
 	 * @return list de Rameurs
 	 */
 	public List<Rameur> getEquipage(String equipage) {
 		List<Rameur> equi = null;
 		if (! equipage.isEmpty()){
 			equi = new ArrayList<Rameur>();
 			for(String e : equipage.split("/")){
 				equi.add(this.dbm.getRameurByNBLicence(e));
 			}
 		}
 		return equi;
 	}
 
 	@Override
 	public String[] getAllBateaux() {
 		List<Bateau> liste = this.dbm.getAllBateaux();
 		String[] allBateaux = new String[liste.size()];
 		for(int i = 0 ; i<liste.size() ; i++) {
 			allBateaux[i] = liste.get(i).getRef() + " : " + liste.get(i).getMarque() + "  " + liste.get(i).getModele();
 		}
 		
 		return allBateaux;
 	}
 
 	@Override
 	public String[] getAllSortieZones() {
 		String[] allSortieZones = new String[ZoneSorties.values().length];
 		int i = 0;
 		for (ZoneSorties v: ZoneSorties.values()) {
 			allSortieZones[i++] = v.toString();
 		}
 		return allSortieZones;
 	}
 
 	@Override
 	public String[] getAllDegatRep() {
 		List<DegatRep> liste = this.dbm.getAllDegatRep(); 
		String[] allDegatReps = new String[liste.size()];
		for(int i = 0 ; i < liste.size() ; i ++){
			allDegatReps[i] = liste.get(i).getRef() + " - " + formatterDate.format(liste.get(i).getDateDegat());
 		}
 		return allDegatReps;
 	}
 
 	@Override
 	public String[] getAllTypesReparation(){
 		return DegatRep.TYPESREPATION;
 	}
 	
 	@Override
 	public String[] getAllImportancesDegat(){
 		return DegatRep.IMPORTANCEDEGAT;
 	}
 	
 	@Override
 	public String[] getAllRameurs(){
 		List<Rameur> liste = this.dbm.getAllRameurs();
 		String[] allRameurs = new String[liste.size()];
 		for(int i = 0 ; i < liste.size() ; i ++){
 			allRameurs[i] = liste.get(i).getNom() + " " + liste.get(i).getPrenom() + " : " + liste.get(i).getLicence();			
 		}
 		return allRameurs;
 	}
 }
 
 
 
