 package org.rhq.plugins.sqlserver;
 
 import org.rhq.core.domain.measurement.AvailabilityType;
 import org.rhq.plugins.database.CustomTableComponent;
 
 public class MSSQLTableComponent extends CustomTableComponent {
 
    public String getTableName() {
        return this.context.getPluginConfiguration().getSimple("tableName").getStringValue();
    }
 
 	@Override
 	public AvailabilityType getAvailability() {
 		return AvailabilityType.UP;
 	}
	
 }
