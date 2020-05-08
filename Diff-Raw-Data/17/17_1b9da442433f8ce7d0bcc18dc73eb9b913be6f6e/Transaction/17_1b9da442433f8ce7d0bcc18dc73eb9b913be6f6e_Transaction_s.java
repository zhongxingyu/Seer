 package edu.ucsb.cs.cs185.moneysaver;
 
 public class Transaction {
 	private long m_id; 
 	private String m_name;
 	private float m_value;
 	private String m_category;
 	private String m_date;
 
 	public Transaction(String name, float value, String category, String date) {
 		m_name = name;
 		m_value = value;
 		m_category = category;
 		m_date = date;
 	}
 	
 /*
  * Getters
  */
 	public String getName() {
 		return m_name;
 	}
 	public float getValue() {
 		return m_value;	
 	}
 	public String getCategory() {
 		return m_category;
 	}
 	public String getDate() {
 		return m_date;
 	}
 	
 	public long getId() {
 		return m_id;
 	}
 /*
  * Setters
  */
 	public void setId(long id) {
 		m_id = id;
 	}
 	
 	public void setName(String name) {
 		m_name = name;
 	}
 	
 	public void setValue(float value) {
 		m_value = value;
 	}
 	
 	public void setCategory(String category) {
 		m_category = category;
 	}
 	
 	public void setDate(String date) {
 		m_date = date;
 	}
 	
 	@Override
 	public String toString() {
 		String ret_str = "ID: " + m_id + " " + m_name + " " + m_value + " " + m_category + " " + m_date;
 		return ret_str;
 	}
 	
 	
 }
