 package kea.kme.pullpit.client.objects;
 
 import java.io.Serializable;
 
 public class Show implements Serializable {
 	private static final long serialVersionUID = -3768259649497315375L;
 	private int showID;
 	private Band band;
 	private Venue[] venues;
 	private String date;
 	private Promoter promoter;
 	private int state;
 	private double fee;
 	private String feeCurrency;
 	private double provision;
 	private String provisionCurrency;
 	private int productionType;
 	private int profitSplit;
 	private int ticketPrice;
 	private double kodaPct;
 	private double VAT;
 	private int ticketsSold;
 	private String comments;
 	private String lastEdit;
 
 	public Show(int showID, Band band, Venue[] venues, String date,
 			int promoID, int state, double fee, String feeCurrency,
 			double provision, String provisionCurrency, int productionType,
 			int profitSplit, int ticketPrice, double kodaPct, double VAT,
 			int ticketsSold, String comments, String lastEdit) {
 		this.showID = showID;
 		this.band = band;
 		this.venues = venues;
 		this.promoter = Promoter.getPromoterByID(promoID);
 		this.date = date;
 		this.state = state;
 		this.fee = fee;
 		this.feeCurrency = feeCurrency;
 		this.provision = provision;
 		this.provisionCurrency = provisionCurrency;
 		this.productionType = productionType;
 		this.profitSplit = profitSplit;
 		this.ticketPrice = ticketPrice;
 		this.kodaPct = kodaPct;
 		this.VAT = VAT;
 		this.ticketsSold = ticketsSold;
 		this.comments = comments;
 		this.lastEdit = lastEdit;
 	}
 
 	public Show() {
 	}
 
 	public Show(int showID, String date, int state, double fee,
 			String feeCurrency, double provision, String provisionCurrency,
 			int productionType, int profitSplit, int ticketPrice,
 			double kodaPct, double VAT, int ticketsSold, String comments,
 			String lastEdit) {
 		this.showID = showID;
 		this.date = date;
 		this.state = state;
 		this.fee = fee;
 		this.feeCurrency = feeCurrency;
 		this.provision = provision;
 		this.provisionCurrency = provisionCurrency;
 		this.productionType = productionType;
 		this.profitSplit = profitSplit;
 		this.ticketPrice = ticketPrice;
 		this.kodaPct = kodaPct;
 		this.VAT = VAT;
 		this.ticketsSold = ticketsSold;
 		this.comments = comments;
 		this.lastEdit = lastEdit;
 	}
 
 	/**
 	 * Used to display
 	 * 
 	 * @param showID
 	 * @param date
 	 * @param state
 	 * @param comments
 	 * @param lastEdit
 	 */
 	public Show(int showID, Band band, String date, int state, String comments,
			String lastEdit, Venue... venues) {
 		this.showID = showID;
 		this.band = band;
 		this.date = date;
 		this.state = state;
 		this.comments = comments;
 		this.lastEdit = lastEdit;
 		this.venues = venues;
 	}
 
 	public int getShowID() {
 		return showID;
 	}
 
 	public void setShowID(int showID) {
 		this.showID = showID;
 	}
 
 	public Band getBand() {
 		return band;
 	}
 
 	public void setBand(Band band) {
 		this.band = band;
 	}
 
 	public Venue[] getVenues() {
 		return venues;
 	}
 
 	public void setVenues(Venue[] venues) {
 		this.venues = venues;
 	}
 
 	public String getVenuesString() {
 		String venueString = "";
 		if (venues != null) {
 			Venue[] venues = this.venues;
 
 			venueString = venues[0].getVenueName();
 
 			for (int i = 1; i < venues.length; i++) {
 				venueString += ", " + venues[i].getVenueName();
 			}
 
 		}
 		return venueString;
 	}
 
 	public String getDate() {
 		return date;
 	}
 
 	public void setDate(String date) {
 		this.date = date;
 	}
 
 	public Promoter getPromoter() {
 		return promoter;
 	}
 
 	public void setPromoter(int promoID) {
 		this.promoter = Promoter.getPromoterByID(promoID);
 	}
 
 	public int getState() {
 		return state;
 	}
 
 	public void setState(int state) {
 		this.state = state;
 	}
 
 	public double getFee() {
 		return fee;
 	}
 
 	public void setFee(double fee) {
 		this.fee = fee;
 	}
 
 	public String getFeeCurrency() {
 		return feeCurrency;
 	}
 
 	public void setFeeCurrency(String feeCurrency) {
 		this.feeCurrency = feeCurrency;
 	}
 
 	public double getProvision() {
 		return provision;
 	}
 
 	public void setProvision(double provision) {
 		this.provision = provision;
 	}
 
 	public String getProvisionCurrency() {
 		return provisionCurrency;
 	}
 
 	public void setProvisionCurrency(String provisionCurrency) {
 		this.provisionCurrency = provisionCurrency;
 	}
 
 	public int getProductionType() {
 		return productionType;
 	}
 
 	public void setProductionType(int productionType) {
 		this.productionType = productionType;
 	}
 
 	public int getProfitSplit() {
 		return profitSplit;
 	}
 
 	public void setProfitSplit(int profitSplit) {
 		this.profitSplit = profitSplit;
 	}
 
 	public int getTicketPrice() {
 		return ticketPrice;
 	}
 
 	public void setTicketPrice(int ticketPrice) {
 		this.ticketPrice = ticketPrice;
 	}
 
 	public double getKodaPct() {
 		return kodaPct;
 	}
 
 	public void setKodaPct(double kodaPct) {
 		this.kodaPct = kodaPct;
 	}
 
 	public double getVAT() {
 		return VAT;
 	}
 
 	public void setVAT(double vAT) {
 		VAT = vAT;
 	}
 
 	public int getTicketsSold() {
 		return ticketsSold;
 	}
 
 	public void setTicketsSold(int ticketsSold) {
 		this.ticketsSold = ticketsSold;
 	}
 
 	public String getComments() {
 		return comments;
 	}
 
 	public void setComments(String comments) {
 		this.comments = comments;
 	}
 
 	public String getLastEdit() {
 		return lastEdit;
 	}
 
 	public void setLastEdit(String lastEdit) {
 		this.lastEdit = lastEdit;
 	}
 
 }
