 package no.runsafe.framework.database;
 
 public interface ISchemaUpdater {
	void Run(SchemaRevisionRepository repository, IDatabase db);
 }
