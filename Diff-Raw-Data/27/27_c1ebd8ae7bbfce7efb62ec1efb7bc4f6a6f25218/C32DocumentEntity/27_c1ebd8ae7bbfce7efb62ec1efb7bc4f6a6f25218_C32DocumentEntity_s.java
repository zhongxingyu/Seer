 package org.osehra.das.wrapper.nwhin;
 
 import java.io.Serializable;
 import java.sql.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Lob;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "C32_DOCUMENT")
 public class C32DocumentEntity implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@GeneratedValue
 	@Column(name = "audit_message_id")
 	private int id;
 
 	@Column(name = "document")
 	@Lob
 	private byte[] document;
 
 	@Column(name = "icn")
 	private String icn ;
 
 	@Column(name = "createDate")
 	private Date createDate;
 
 	@Column(name = "DOCUMENT_PATIENT_ID")
 	private String documentPatientId;	
 	
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setDocument(String document) {
		C32DocumentLogic logic = new C32DocumentLogic();
		String filteredDocument = logic.filterDocument(document);
		this.document = filteredDocument.getBytes();
 	}
 
 	public String getDocument() {
 		return document.toString();
 	}
 
 	public Date getCreateDate() {
 		return createDate;
 	}
 
 	public void setCreateDate(Date createDate) {
 		this.createDate = createDate;
 	}
 
 	public String getIcn() {
 		return icn;
 	}
 
 	public void setIcn(String icn) {
 		this.icn = icn;
 	}
 
 	public void setDocumentPatientId(String document) {
 		C32DocumentLogic pidLogic = new C32DocumentLogic();
 		String documentPatientId = pidLogic.getPatientId(document);
 		this.documentPatientId = documentPatientId;
 	}
 
 	public String getDocumentPatientId() {
 		return documentPatientId;
 	}	
 	
 	@Override
 	public String toString() {
 		return "C32DocumentEntity{" + "id=" + id + ", document=" + document
 				+ '}';
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof C32DocumentEntity)) {
 			return false;
 		}
 		C32DocumentEntity other = (C32DocumentEntity)obj;
 		return equalsNullSafe(this.getId(), other.getId());
 	}
 	
 	@Override
 	public int hashCode() {
 		return hashCodeNullSafe(getId());
 	}
 
 	protected static boolean equalsNullSafe(Object item1, Object item2) {
 		if (item1==null && item2==null) {
 			return true;
 		}
 		if (item1!=null) {
 			return item1.equals(item2);
 		}
 		return item2.equals(item1);
 	}
 	
 	protected static int hashCodeNullSafe(Object item) {
 		if (item==null) {
 			return 0;
 		}
 		return item.hashCode();
 	}
 
 }
