 package no.runsafe.framework.database;
 
 public interface ISchemaUpdater {
	public void Run(SchemaRevisionRepository repository, IDatabase db);
 }
