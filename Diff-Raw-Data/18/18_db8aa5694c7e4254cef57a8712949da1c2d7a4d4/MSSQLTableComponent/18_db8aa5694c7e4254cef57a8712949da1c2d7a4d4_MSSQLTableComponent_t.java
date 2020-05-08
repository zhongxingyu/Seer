 package org.rhq.plugins.sqlserver;
 
import org.rhq.core.pluginapi.inventory.ResourceContext;
 import org.rhq.core.domain.measurement.AvailabilityType;
 import org.rhq.plugins.database.CustomTableComponent;
 
 public class MSSQLTableComponent extends CustomTableComponent {
 
    private ResourceContext<MSSQLDatabaseComponent<?>> resourceContext;

	@Override
	public void start(ResourceContext<MSSQLDatabaseComponent<?>> resourceContext) {
		this.resourceContext = resourceContext;
	}

	public String getTableName() {
		return this.resourceContext.getPluginConfiguration().getSimple("tableName").getStringValue();
	}
 
 	@Override
 	public AvailabilityType getAvailability() {
 		return AvailabilityType.UP;
 	}

 }
