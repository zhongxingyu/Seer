 package com.iappsam.forms;
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 import com.iappsam.DivisionOffice;
 import com.iappsam.Employee;
 import com.iappsam.Item;
 import com.iappsam.Supplier;
 
 @Entity
 @Table(name = "Purchase_Order")
 public class PO implements Form {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Column(name = "id")
 	private int id;
 
 	@Column(name = "PO_Number")
 	private String poNumber;
 
 	@ManyToOne
 	@JoinColumn(name = "Supplier_ID")
 	private Supplier supplier;
 
 	@Column(name = "Date")
 	private Date date;
 
 	@ManyToOne
 	@JoinColumn(name = "Mode_Of_Procurement_ID")
 	private ModeOfProcurement modeOfProcurement;
 
 	@ManyToOne
 	@JoinColumn(name = "DivisionOffice_ID")
 	private DivisionOffice divisionOffice;
 
 	@Column(name = "Date_Of_Delivery")
 	private Date dateOfDelivery;
 
 	@Column(name = "Payment_Term")
 	private String paymentTerm;
 
 	@Column(name = "Delivery_Term")
 	private String deliveryTerm;
 
 	@Column(name = "Total_Amount_In_Words")
 	private String totalAmountInWords;
 
 	@Column(name = "OR_Number")
 	private String orNumber;
 
 	@Column(name = "Amount")
 	private float amount;
 
 	@ManyToOne
 	@JoinColumn(name = "Supplier_Name_ID")
 	private Employee supplierName;
 
 	@Column(name = "Supplier_Date")
 	private Date supplierDate;
 
 	@ManyToOne
 	@JoinColumn(name = "Accountant_ID")
 	private Employee accountant;
 
 	@Column(name = "Accountant_Date")
 	private Date accountantDate;
 
 	@ManyToOne
 	@JoinColumn(name = "Dean_ID")
 	private Employee dean;
 
 	@Column(name = "Dean_Date")
 	private Date deanDate;
 
 	@OneToMany(fetch = FetchType.EAGER, mappedBy = "po", cascade = CascadeType.ALL)
 	private Set<POLine> lines = new HashSet<POLine>();
 
 	public PO() {
 	}
 
 	public PO(String poNumber, Supplier supplier, Date date, ModeOfProcurement modeOfProcurement, DivisionOffice divisionOffice, Date dateOfDelivery,
 			Employee supplierName, Date supplierDate, Employee accountant, Date accountantDate, Employee dean, Date deanDate) {
 		super();
 		this.poNumber = poNumber;
 		this.supplier = supplier;
 		this.date = date;
 		this.modeOfProcurement = modeOfProcurement;
 		this.divisionOffice = divisionOffice;
 		this.dateOfDelivery = dateOfDelivery;
 		this.supplierName = supplierName;
 		this.supplierDate = supplierDate;
 		this.accountant = accountant;
 		this.accountantDate = accountantDate;
 		this.dean = dean;
 		this.deanDate = deanDate;
 	}
 
 	public String getPoNumber() {
 		return poNumber;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public Date getDateOfDelivery() {
 		return dateOfDelivery;
 	}
 
 	public String getPaymentTerm() {
 		return paymentTerm == null ? "" : paymentTerm;
 	}
 
 	public String getDeliveryTerm() {
 		return deliveryTerm == null ? "" : deliveryTerm;
 	}
 
 	public String getTotalAmountInWords() {
 		return totalAmountInWords == null ? "" : totalAmountInWords;
 	}
 
 	public String getOrNumber() {
 		return orNumber == null ? "" : orNumber;
 	}
 
 	public float getAmount() {
 		return amount;
 	}
 
 	public void setPoNumber(String poNumber) {
 		this.poNumber = poNumber;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public DivisionOffice getDivisionOffice() {
 		return divisionOffice;
 	}
 
 	public void setDivisionOffice(DivisionOffice divisionOffice) {
 		this.divisionOffice = divisionOffice;
 	}
 
 	public void setDateOfDelivery(Date dateOfDelivery) {
 		this.dateOfDelivery = dateOfDelivery;
 	}
 
 	public void setDateOfDelivery(String parameter) {
 		try {
 			setDateOfDelivery(Date.valueOf(parameter));
 		} catch (Exception e) {
 		}
 	}
 
 	public void setPaymentTerm(String paymentTerm) {
 		this.paymentTerm = paymentTerm;
 	}
 
 	public Supplier getSupplier() {
 		return supplier;
 	}
 
 	public void setSupplier(Supplier supplier) {
 		this.supplier = supplier;
 	}
 
 	public ModeOfProcurement getModeOfProcurement() {
 		return modeOfProcurement;
 	}
 
 	public void setModeOfProcurement(ModeOfProcurement modeOfProcurement) {
 		this.modeOfProcurement = modeOfProcurement;
 	}
 
 	public Employee getSupplierName() {
 		return supplierName;
 	}
 
 	public void setSupplierName(Employee supplierName) {
 		this.supplierName = supplierName;
 	}
 
 	public Employee getAccountant() {
 		return accountant;
 	}
 
 	public void setAccountant(Employee accountant) {
 		this.accountant = accountant;
 	}
 
 	public Date getSupplierDate() {
 		return supplierDate;
 	}
 
 	public void setSupplierDate(Date supplierDate) {
 		this.supplierDate = supplierDate;
 	}
 
 	public Date getAccountantDate() {
 		return accountantDate;
 	}
 
 	public void setAccountantDate(Date accountantDate) {
 		this.accountantDate = accountantDate;
 	}
 
 	public void setAccountantDate(String parameter) {
 		try {
 			setAccountantDate(Date.valueOf(parameter));
 		} catch (Exception e) {
 		}
 	}
 
 	public Date getDeanDate() {
 		return deanDate;
 	}
 
 	public void setDeanDate(Date deanDate) {
 		this.deanDate = deanDate;
 	}
 
 	public void setDeanDate(String parameter) {
 		try {
 			setDeanDate(Date.valueOf(parameter));
 		} catch (Exception e) {
 		}
 	}
 
 	public Employee getDean() {
 		return dean;
 	}
 
 	public void setDean(Employee dean) {
 		this.dean = dean;
 	}
 
 	public void setDeliveryTerm(String deliveryTerm) {
 		this.deliveryTerm = deliveryTerm;
 	}
 
 	public void setTotalAmountInWords(String totalAmountInWords) {
 		this.totalAmountInWords = totalAmountInWords;
 	}
 
 	public void setOrNumber(String orNumber) {
 		this.orNumber = orNumber;
 	}
 
 	public void setAmount(String amountParam) {
		setAmount(checkNum(amountParam));
	}

	private float checkNum(String value) {
		float num;
 		try {
			num = Float.parseFloat(value);
			if (num < 0)
				return 0;
 		} catch (Exception e) {
			return 0;
 		}
		return num;
 	}
 
 	private void setAmount(float amount) {
 		this.amount = amount;
 	}
 
 	public void addLine(Item item, String quantity) {
 		lines.add(new POLine(this, item, quantity));
 	}
 
 	public void addLine(POLine line) {
 		lines.add(line);
 	}
 
 	public void removeLine(Item item) {
 		POLine remove = null;
 
 		for (POLine line : lines)
 			if (line.getItem().equals(item))
 				remove = line;
 
 		lines.remove(remove);
 	}
 
 	public void removeLine(POLine line) {
 		lines.remove(line);
 	}
 
 	public Set<POLine> getLines() {
 		return lines;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((accountant == null) ? 0 : accountant.hashCode());
 		result = prime * result + ((accountantDate == null) ? 0 : accountantDate.hashCode());
 		result = prime * result + ((date == null) ? 0 : date.hashCode());
 		result = prime * result + ((dateOfDelivery == null) ? 0 : dateOfDelivery.hashCode());
 		result = prime * result + ((dean == null) ? 0 : dean.hashCode());
 		result = prime * result + ((deanDate == null) ? 0 : deanDate.hashCode());
 		result = prime * result + ((divisionOffice == null) ? 0 : divisionOffice.hashCode());
 		result = prime * result + ((modeOfProcurement == null) ? 0 : modeOfProcurement.hashCode());
 		result = prime * result + ((poNumber == null) ? 0 : poNumber.hashCode());
 		result = prime * result + ((supplier == null) ? 0 : supplier.hashCode());
 		result = prime * result + ((supplierDate == null) ? 0 : supplierDate.hashCode());
 		result = prime * result + ((supplierName == null) ? 0 : supplierName.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		PO other = (PO) obj;
 		if (accountant == null) {
 			if (other.accountant != null)
 				return false;
 		} else if (!accountant.equals(other.accountant))
 			return false;
 		if (accountantDate == null) {
 			if (other.accountantDate != null)
 				return false;
 		} else if (!accountantDate.equals(other.accountantDate))
 			return false;
 		if (date == null) {
 			if (other.date != null)
 				return false;
 		} else if (!date.equals(other.date))
 			return false;
 		if (dateOfDelivery == null) {
 			if (other.dateOfDelivery != null)
 				return false;
 		} else if (!dateOfDelivery.equals(other.dateOfDelivery))
 			return false;
 		if (dean == null) {
 			if (other.dean != null)
 				return false;
 		} else if (!dean.equals(other.dean))
 			return false;
 		if (deanDate == null) {
 			if (other.deanDate != null)
 				return false;
 		} else if (!deanDate.equals(other.deanDate))
 			return false;
 		if (divisionOffice == null) {
 			if (other.divisionOffice != null)
 				return false;
 		} else if (!divisionOffice.equals(other.divisionOffice))
 			return false;
 		if (modeOfProcurement == null) {
 			if (other.modeOfProcurement != null)
 				return false;
 		} else if (!modeOfProcurement.equals(other.modeOfProcurement))
 			return false;
 		if (poNumber == null) {
 			if (other.poNumber != null)
 				return false;
 		} else if (!poNumber.equals(other.poNumber))
 			return false;
 		if (supplier == null) {
 			if (other.supplier != null)
 				return false;
 		} else if (!supplier.equals(other.supplier))
 			return false;
 		if (supplierDate == null) {
 			if (other.supplierDate != null)
 				return false;
 		} else if (!supplierDate.equals(other.supplierDate))
 			return false;
 		if (supplierName == null) {
 			if (other.supplierName != null)
 				return false;
 		} else if (!supplierName.equals(other.supplierName))
 			return false;
 		return true;
 	}
 
 	@Override
 	public boolean validate() {
 		boolean validDivisionOffice = divisionOffice != null && divisionOffice.validate();
 		boolean validMop = modeOfProcurement != null && modeOfProcurement.validate();
 		boolean validDate = date != null;
 		boolean validPoNumber = poNumber != null && !poNumber.isEmpty();
 		boolean validSupplier = supplier != null && supplier.validate();
 		boolean validDateOfDelivery = dateOfDelivery != null;
 		boolean validSupplierName = supplierName != null && supplierName.validate();
 		boolean validSupplierDate = supplierDate != null;
 		boolean validAccountant = accountant != null && accountant.validate();
 		boolean validAccountantDate = accountantDate != null;
 		boolean validDean = dean != null && dean.validate();
 		boolean validDeanDate = deanDate != null;
		boolean validAmount = amount >= 0;
 		boolean validLines = !lines.isEmpty();
 
 		for (POLine line : lines)
 			validLines &= line.validate();
 
 		return validPoNumber && validSupplier && validDate && validMop && validDivisionOffice && validDateOfDelivery && validSupplierName
				&& validSupplierDate && validAccountant && validAccountantDate && validDean && validDeanDate && validAmount && validLines;
 	}
 
 	@Override
 	public List<Item> getItems() {
 		List<Item> items = new ArrayList<Item>();
 
 		for (POLine line : lines)
 			items.add(line.getItem());
 
 		return items;
 	}
 
 	@Override
 	public void addItem(Item item) {
 		addLine(item, "0");
 	}
 
 	@Override
 	public void removeItem(Item item) {
 		POLine remove = null;
 
 		for (POLine line : lines)
 			if (line.getItem().equals(item))
 				remove = line;
 
 		lines.remove(remove);
 	}
 
 	@Override
 	public int getId() {
 		return id;
 	}
 
 	public void setSupplierDate(String parameter) {
 		try {
 			setSupplierDate(java.sql.Date.valueOf(parameter));
 		} catch (Exception e) {
 		}
 	}
 
 	public void setDate(String parameter) {
 		try {
 			setDate(java.sql.Date.valueOf(parameter));
 		} catch (Exception e) {
 		}
 	}
 
 	public float getTotalAmount() {
 		float totalAmount = 0;
 
 		for (POLine line : lines)
 			totalAmount += line.getAmount();
 
 		return totalAmount;
 	}
 }
