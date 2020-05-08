 package com.gemantic.killer.common.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
import java.util.HashMap;
 import java.util.List;
import java.util.Map;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 
 /**
  * 一次操作.
  * 输入是一个Message
  * 输出是两个Message的List. 
  * 立即执行的Message和定时执行的Message
  * @author Administrator
  *
  */
 public class Operater implements Serializable{
 
 
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6463528748935721309L;
 	
 	
 	
 	private Long id;
 	
 	private Message message;
 	private List<Message> nextMessages=new ArrayList();
 	private List<Message> timerMessages=new ArrayList();
 	
 	private String snapshots;
 	
 	private Boolean gameOver=false;
 	
 	private Boolean gameStart=false;
 	
 	private Boolean roomEmpty=false;
 	
 	private Setting setting;
 	
 	private Long recordID;
 	
 	
 	private List<Long> players=new ArrayList();
 	
 	
	private Map<Long,Integer> money=new HashMap();
	
 	
 	
 	
 	
 
 	
 	
 	
 
 	
 	
 
 
 
 	
 	
 
 
 	public List<Long> getPlayers() {
 		return players;
 	}
 
 
 
 	public void setPlayers(List<Long> players) {
 		this.players = players;
 	}
 
 
 
 	public Long getRecordID() {
 		return recordID;
 	}
 
 
 
 	public void setRecordID(Long recordID) {
 		this.recordID = recordID;
 	}
 
 
 
 	public Long getId() {
 		return id;
 	}
 
 
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 
 
 	public Setting getSetting() {
 		return setting;
 	}
 
 
 
 	public void setSetting(Setting setting) {
 		this.setting = setting;
 	}
 
 
 
 	public Operater() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	
 	
 	public Operater(Message message) {
 		super();
 		this.message=message;
 		this.id=message.getId();
 	}
 
 
 	
 	
 
 
 	public Message getMessage() {
 		return message;
 	}
 
 
 
 
 
 
 
 
 
 	public void setMessage(Message message) {
 		this.message = message;
 	}
 
 
 
 
 
 
 
 
 
 	public List<Message> getNextMessages() {
 		return nextMessages;
 	}
 
 
 
 
 
 
 
 
 
 	public void setNextMessages(List<Message> nextMessages) {
 		this.nextMessages = nextMessages;
 	}
 
 
 
 
 
 
 
 
 
 	public List<Message> getTimerMessages() {
 		return timerMessages;
 	}
 
 
 
 
 
 
 	public void setTimerMessages(List<Message> timerMessages) {
 		this.timerMessages = timerMessages;
 	}
 	
 	
 	
 
 
 	public String getSnapshots() {
 		return snapshots;
 	}
 
 
 
 	public void setSnapshots(String snapshots) {
 		this.snapshots = snapshots;
 	}
 	
 	
 	
 
 
 
 	public Boolean getGameOver() {
 		return gameOver;
 	}
 
 
 
 	public void setGameOver(Boolean gameOver) {
 		this.gameOver = gameOver;
 	}
 	
 	
 	
 
 
 
 	public Boolean getRoomEmpty() {
 		return roomEmpty;
 	}
 
 
 
 	public void setRoomEmpty(Boolean roomEmpty) {
 		this.roomEmpty = roomEmpty;
 	}
 	
 	
 	
 
 
 
 	public Boolean getGameStart() {
 		return gameStart;
 	}
 
 
 
 	public void setGameStart(Boolean gameStart) {
 		this.gameStart = gameStart;
 	}
	
	
	
	



	public Map<Long, Integer> getMoney() {
		return money;
	}



	public void setMoney(Map<Long, Integer> money) {
		this.money = money;
	}
 
 
 
 	public String toString() {
 		return ToStringBuilder.reflectionToString(this,
 				ToStringStyle.MULTI_LINE_STYLE);
 	}
 
 }
