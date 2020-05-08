 package nz.ac.vuw.ecs.rprofs.server.db;
 
import java.util.Date;

import javax.validation.constraints.NotNull;
 import nz.ac.vuw.ecs.rprofs.server.data.util.DatasetCreator;
 import nz.ac.vuw.ecs.rprofs.server.data.util.DatasetQuery;
 import nz.ac.vuw.ecs.rprofs.server.data.util.DatasetUpdater;
 import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
 import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
 
 /**
  * Author: Stephen Nelson <stephen@sfnelson.org>
  * Date: 12/09/11
  */
 abstract class MongoDatasetBuilder extends MongoBuilder<MongoDatasetBuilder, DatasetId, Dataset>
 		implements DatasetCreator<MongoDatasetBuilder>, DatasetUpdater<MongoDatasetBuilder>, DatasetQuery<MongoDatasetBuilder> {
 
 	@Override
 	@NotNull
 	public MongoDatasetBuilder init(Dataset dataset) {
 		b.append("_id", dataset.getId());
 		b.append("version", dataset.getVersion());
 		setHandle(dataset.getHandle());
 		setStarted(dataset.getStarted());
 		if (dataset.getProgram() != null) {
 			setProgram(dataset.getProgram());
 		}
 		if (dataset.getStopped() != null) {
 			setStopped(dataset.getStopped());
 		}
 		return this;
 	}
 
 	@Override
 	@NotNull
 	public MongoDatasetBuilder setHandle(String handle) {
 		b.append("handle", handle);
 		return this;
 	}
 
 	@Override
 	@NotNull
 	public MongoDatasetBuilder setStarted(Date date) {
 		b.append("started", date);
 		return this;
 	}
 
 	@Override
 	@NotNull
 	public MongoDatasetBuilder setStopped(Date date) {
 		b.append("stopped", date);
 		return this;
 	}
 
 	@Override
 	@NotNull
 	public MongoDatasetBuilder setProgram(String program) {
 		b.append("program", program);
 		return this;
 	}
 
 	@Override
 	@NotNull
 	public Dataset get() {
 		DatasetId id = new DatasetId(((Long) b.get("_id")).shortValue());
 		Integer version = (Integer) b.get("version");
 		String handle = (String) b.get("handle");
 		Date started = (Date) b.get("started");
 		Dataset dataset = new Dataset(id, version, handle, started);
 		if (b.containsField("stopped")) {
 			dataset.setStopped((Date) b.get("stopped"));
 		}
 		if (b.containsField("program")) {
 			dataset.setProgram((String) b.get("program"));
 		}
 		reset();
 		return dataset;
 	}
 }
