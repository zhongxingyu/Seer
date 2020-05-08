 package org.genericsystem.systemproperties.constraints;
 
 import java.io.Serializable;
 
 import org.genericsystem.annotation.SystemGeneric;
 import org.genericsystem.core.Generic;
 import org.genericsystem.core.GenericImpl;
 import org.genericsystem.exception.ConstraintViolationException;
 import org.genericsystem.map.ConstraintsMapProvider.MapInstance;
 
 public abstract class AbstractAxedConstraintImpl extends AbstractConstraintImpl {
 
	@SuppressWarnings("unchecked")
 	public AbstractAxedConstraintImpl bindAxedConstraint(int pos) {
		Generic implicit = getEngine().bindPrimary(Generic.class, new AxedConstraintClass((Class<Serializable>) getClass(), pos), SystemGeneric.STRUCTURAL, true);
 		return getCurrentCache().<GenericImpl> find(MapInstance.class).bind(getClass(), implicit, this, getBasePos(this), false, new Generic[] {});
 	}
 
	@SuppressWarnings("unchecked")
 	public AbstractAxedConstraintImpl findAxedConstraint(int pos) {
		Generic implicit = getEngine().findPrimary(new AxedConstraintClass((Class<Serializable>) getClass(), pos), SystemGeneric.STRUCTURAL);
 		if (implicit == null)
 			return null;
 		return getCurrentCache().<GenericImpl> find(MapInstance.class).<AbstractAxedConstraintImpl> find(implicit, this, getBasePos(this), new Generic[] {});
 	}
 
 	public abstract void check(Generic baseComponent, Generic modified, int axe) throws ConstraintViolationException;
 
 	public static class AxedConstraintClass implements Serializable {
 		private static final long serialVersionUID = 182492104604984855L;
 
		private final Class<Serializable> clazz;
 		private final int axe;
 
		public AxedConstraintClass(Class<Serializable> clazz, int axe) {
 			this.clazz = clazz;
 			this.axe = axe;
 		}
 
		public Class<Serializable> getClazz() {
 			return clazz;
 		}
 
 		public int getAxe() {
 			return axe;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (!(obj instanceof AxedConstraintClass))
 				return false;
 			AxedConstraintClass compare = (AxedConstraintClass) obj;
 			return clazz.equals(compare.getClazz()) && axe == compare.axe;
 		}
 
 		@Override
 		public int hashCode() {
 			return clazz.hashCode();
 		}
 
 		@Override
 		public String toString() {
 			return "class : " + clazz + ", axe : " + axe;
 		}
 	}
 
 }
