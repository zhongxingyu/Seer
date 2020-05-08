 package org.ocha.hdx.model.validation;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.ocha.hdx.persistence.entity.ckan.CKANDataset;
 
 public class ValidationReport implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6222487518749118335L;
 
 	private ValidationStatus status;
 
 	public boolean isNotInError() {
 		return !status.equals(ValidationStatus.ERROR);
 	}
 
	private final CKANDataset.Type validator;
 
 	private final List<ValidationReportEntry> entries;
 
 	public ValidationReport(final CKANDataset.Type validator) {
 		super();
 		status = ValidationStatus.SUCCESS;
 		this.validator = validator;
 		entries = new ArrayList<>();
 	}
 
 	public void addEntry(final ValidationStatus newEntryStatus, final String newEntryMessage) {
 		// Compare to uses the ordinal of the Enum (based on declaration order)
 		if (newEntryStatus.compareTo(status) > 0) {
 			status = newEntryStatus;
 		}
 		final ValidationReportEntry newEntry = new ValidationReportEntry(newEntryStatus, newEntryMessage);
 		entries.add(newEntry);
 	}
 
 	public void addEntries(final List<ValidationReportEntry> newEntries) {
 		for (final ValidationReportEntry newEntry : newEntries) {
 			addEntry(newEntry.getStatus(), newEntry.getMessage());
 		}
 	}
 
 	public ValidationStatus getStatus() {
 		return status;
 	}
 
 	public CKANDataset.Type getValidator() {
 		return validator;
 	}
 
	public List<ValidationReportEntry> getEntries() {
 		return entries;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "ValidationReport [status=" + status + ", validator=" + validator + ", entries=" + entries + "]";
 	}
 	
 	
 
 }
