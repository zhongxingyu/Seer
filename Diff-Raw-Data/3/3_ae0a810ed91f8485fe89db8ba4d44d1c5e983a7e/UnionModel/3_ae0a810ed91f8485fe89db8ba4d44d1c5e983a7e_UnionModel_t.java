 package org.openrdf.model.impl;
 
 import java.util.Iterator;
 import java.util.Map;
 
 import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 
 public class UnionModel extends AbstractModel {
 	private static final long serialVersionUID = -2735528661997692589L;
 
 	private class UnionIterator implements Iterator<Statement> {
 		private final Model[] models;
 		private Iterator<Statement> iter;
 		private int idx = -1;
 
 		public UnionIterator(Model[] models) {
 			this.models = models;
 		}
 
 		public boolean hasNext() {
 			if (iter == null) {
 				iter = models[++idx].iterator();
			}
			while (!iter.hasNext() && idx < models.length - 1) {
 				iter = models[++idx].iterator();
 			}
 			return iter.hasNext();
 		}
 
 		public Statement next() {
 			hasNext();
 			return iter.next();
 		}
 
 		public void remove() {
 			if (iter == null)
 				throw new IllegalStateException(
 						"next() must be called before models()");
 			iter.remove();
 		}
 	}
 
 	private final Model add;
 	private final Model[] models;
 
 	public UnionModel(Model... unionOf) {
 		if (unionOf == null || unionOf.length == 0) {
 			add = new EmptyModel(new LinkedHashModel());
 			models = new Model[] { add };
 		} else {
 			add = unionOf[0];
 			models = unionOf;
 		}
 	}
 
 	public Map<String, String> getNamespaces() {
 		return add.getNamespaces();
 	}
 
 	public String getNamespace(String prefix) {
 		return add.getNamespace(prefix);
 	}
 
 	public String setNamespace(String prefix, String name) {
 		String ret = null;
 		for (Model model : models) {
 			ret = model.setNamespace(prefix, name);
 		}
 		return ret;
 	}
 
 	public String removeNamespace(String prefix) {
 		String ret = null;
 		for (Model model : models) {
 			ret = model.removeNamespace(prefix);
 		}
 		return ret;
 	}
 
 	public boolean contains(Resource subj, URI pred, Value obj,
 			Resource... contexts) {
 		for (Model model : models) {
 			if (model.contains(subj, pred, obj, contexts))
 				return true;
 		}
 		return false;
 	}
 
 	public boolean add(Resource subj, URI pred, Value obj, Resource... contexts) {
 		return add.add(subj, pred, obj, contexts);
 	}
 
 	public boolean remove(Resource subj, URI pred, Value obj,
 			Resource... contexts) {
 		boolean modified = false;
 		for (Model model : models) {
 			modified |= model.remove(subj, pred, obj, contexts);
 		}
 		return modified;
 	}
 
 	public Model filter(Resource subj, URI pred, Value obj,
 			Resource... contexts) {
 		final Model[] filter = new Model[models.length];
 		for (int i = 0; i < filter.length; i++) {
 			filter[i] = models[i].filter(subj, pred, obj, contexts);
 		}
 		return new UnionModel(filter);
 	}
 
 	@Override
 	public Iterator<Statement> iterator() {
 		return new UnionIterator(models);
 	}
 
 	@Override
 	public int size() {
 		int size = 0;
 		for (Model model : models) {
 			size += model.size();
 		}
 		return size;
 	}
 
 	@Override
 	protected void removeIteration(Iterator<Statement> union, Resource subj,
 			URI pred, Value obj, Resource... contexts) {
 		Iterator<Statement> iter = ((UnionIterator) union).iter;
 		int idx = ((UnionIterator) union).idx;
 		for (int i = 0; i < models.length; i++) {
 			Model model = models[i];
 			if (i == idx) {
 				iter.remove();
 			} else {
 				model.remove(subj, pred, obj, contexts);
 			}
 		}
 	}
 
 }
