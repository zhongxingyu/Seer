 package com.myrontuttle.fin.trade.adapt;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 
 import com.myrontuttle.fin.trade.api.SelectedStrategyParameter;
 
 @Entity(name = "TRADE_INSTRUCTIONS")
 public class TradeInstruction implements Serializable, SelectedStrategyParameter {
 
 	private static final long serialVersionUID = 1L;
 	public static final String CATEGORY = ":";
 	public static final String ASSIGN = "=";
 	
 	@Id
 	@Column(name = "TRADE_INSTRUCTION_ID", nullable = false)
     @GeneratedValue(strategy = GenerationType.AUTO)
 	private String tradeInstructionId;
 
 	@Column(name = "TRADER_ID")
 	private String traderId;
 	
 	@ManyToOne(optional = false)
 	@JoinColumn(name = "TRADER_ID", referencedColumnName = "TRADER_ID")
 	private Trader trader;
 
	@Column(name = "TRADE_ID")
 	private String tradeId;
 	
	@Column(name = "NAME")
 	private String name;
 	
	@Column(name = "VALUE")
 	private int value;
 	
 	@Column(name = "INSTRUCTION")
 	private String instruction;
 	
 	public TradeInstruction() {}
 	
 	public TradeInstruction(String traderId, String tradeId, String name, int value) {
 		this.traderId = traderId;
 		this.tradeId = tradeId;
 		this.name = name;
 		this.value = value;
 		this.instruction = generateInstruction(tradeId, name, value);
 	}
 
 	public static String generateInstruction(String tradeId, String name, int value) {
 		return tradeId + CATEGORY + name + ASSIGN + value;
 	}
 
 	public String getTradeInstructionId() {
 		return tradeInstructionId;
 	}
 
 	public void setTradeInstructionId(String tradeInstructionId) {
 		this.tradeInstructionId = tradeInstructionId;
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
 
 	public String getTradeId() {
 		return tradeId;
 	}
 
 	public void setTradeId(String tradeId) {
 		this.tradeId = tradeId;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public int getValue() {
 		return value;
 	}
 
 	public void setValue(int value) {
 		this.value = value;
 	}
 
 	public String getInstruction() {
 		return instruction;
 	}
 
 	public void setInstruction(String instruction) {
 		this.instruction = instruction;
 	}
 }
