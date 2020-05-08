 package com.myrontuttle.fin.trade.adapt;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 
 import com.myrontuttle.fin.trade.api.SelectedAlert;
 
 @Entity(name = "SAVED_ALERTS")
 public class SavedAlert implements Serializable, SelectedAlert {
 
 	private static final long serialVersionUID = 1L;
 	public static final String SEPARATOR = ",";
 	
 	@Id
 	@Column(name = "SAVED_ALERT_ID", nullable = false)
     @GeneratedValue(strategy = GenerationType.AUTO)
 	private String savedAlertId;
 
 	@Column(name = "TRADER_ID")
 	private String traderId;
 	
 	@ManyToOne(optional = false)
 	@JoinColumn(name = "TRADER_ID", referencedColumnName = "TRADER_ID")
 	private Trader trader;
 
 	@Column(name = "ALERT_ID")
 	private int alertId;
 	
 	@Column(name = "CONDITION")
 	private String condition;
 
 	@Column(name = "SYMBOL")
 	private String symbol;
 
 	@Column(name = "PARAM_STRING")
 	private String paramString;
 	
 	private double[] params;
 	
 	public SavedAlert() {}
 
 	public SavedAlert(String traderId, int alertId, String condition, String symbol, double... params) {
 		this.traderId = traderId;
 		this.alertId = alertId;
 		this.condition = condition;
 		this.symbol = symbol;
 		this.params = params;
 		this.paramString = generateParams(params);
 	}
 	
 	public static String generateParams(double[] p) {
 		if (p == null || p.length == 0) {
 			return "";
 		}
 		int k = p.length;
 		StringBuilder out = new StringBuilder();
 		out.append(p[0]);
 		for (int i = 1; i < k; i++) {
 			out.append(SEPARATOR).append(p[i]);
 		}
 
 		return out.toString();
 	}
 	
 	public static double[] parseParams(String params) {
 		String[] asStrings = params.split(SEPARATOR);
 		double[] array = new double[asStrings.length];
 		for (int i=0; i<array.length; i++) {
 			array[i] = Double.parseDouble(asStrings[i]);
 		}
 		return array;
 	}
 
 	public String getSavedAlertId() {
 		return savedAlertId;
 	}
 
 	public void setSavedAlertId(String savedAlertId) {
 		this.savedAlertId = savedAlertId;
 	}
 
 	public String getTraderId() {
 		return traderId;
 	}
 
 	public void setTraderId(String traderId) {
 		this.traderId = traderId;
 	}
 
 	public Trader getTrader() {
 		return trader;
 	}
 
 	public void setTrader(Trader trader) {
 		this.trader = trader;
 	}
 
 	public int getAlertId() {
 		return alertId;
 	}
 
 	public void setAlertId(int alertId) {
 		this.alertId = alertId;
 	}
 
 	public String getCondition() {
 		return condition;
 	}
 
 	public void setCondition(String condition) {
 		this.condition = condition;
 	}
 
 	public String getSymbol() {
 		return symbol;
 	}
 
 	public void setSymbol(String symbol) {
 		this.symbol = symbol;
 	}
 
 	public String getParamString() {
 		return paramString;
 	}
 
 	public void setParamString(String paramString) {
 		this.paramString = paramString;
 	}
 	
 	public double getParam(int index) {
 		return params[index];
 	}
 
 	public double[] getParams() {
 		return params;
 	}
 
 	public void setParams(double[] params) {
 		this.params = params;
 	}
 }
