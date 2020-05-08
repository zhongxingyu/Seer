 package org.genericsystem.constraints;
 
 import java.util.Iterator;
 import org.genericsystem.annotation.Components;
 import org.genericsystem.annotation.Dependencies;
 import org.genericsystem.annotation.Extends;
 import org.genericsystem.annotation.Meta;
 import org.genericsystem.annotation.SystemGeneric;
 import org.genericsystem.annotation.value.AxedConstraintValue;
 import org.genericsystem.annotation.value.BooleanValue;
 import org.genericsystem.constraints.AbstractConstraintImpl.AbstractBooleanNoAxedConstraintImpl;
 import org.genericsystem.core.Generic;
 import org.genericsystem.core.GenericImpl;
 import org.genericsystem.core.Statics;
 import org.genericsystem.exception.ConstraintViolationException;
 import org.genericsystem.exception.UniqueStructuralValueConstraintViolationException;
 import org.genericsystem.generic.Holder;
 import org.genericsystem.map.ConstraintsMapProvider;
 import org.genericsystem.map.ConstraintsMapProvider.ConstraintKey;
 
 /**
  * @author Nicolas Feybesse
  * 
  */
 @SystemGeneric
 @Extends(ConstraintKey.class)
 @Components(ConstraintsMapProvider.class)
 @Dependencies({ StructuralNamingConstraintImpl.DefaultKey.class, StructuralNamingConstraintImpl.DefaultValue.class })
 public class StructuralNamingConstraintImpl extends AbstractBooleanNoAxedConstraintImpl implements Holder {
 
 	@SystemGeneric
 	@Meta(StructuralNamingConstraintImpl.class)
 	@Components(ConstraintsMapProvider.class)
 	@AxedConstraintValue(StructuralNamingConstraintImpl.class)
 	public static class DefaultKey {}
 
 	@SystemGeneric
 	@Meta(ConstraintsMapProvider.ConstraintValue.class)
 	@Components(DefaultKey.class)
 	@BooleanValue(true)
 	public static class DefaultValue {}
 
 	@Override
 	public void check(Generic constraintBase, Generic modified) throws ConstraintViolationException {
 		if (!modified.isStructural())
 			return;
 		if (modified.getComponents().isEmpty()) {
 			Iterator<Generic> iterator = Statics.valueFilter(((GenericImpl) modified.getEngine()).allInstancesIterator(), modified.getValue());
 			if (iterator.hasNext()) {
 				Generic next = iterator.next();
 				if (iterator.hasNext())
 					throw new UniqueStructuralValueConstraintViolationException(next.info() + iterator.next().info());
 			}
 		} else
 			for (int i = 0; i < modified.getComponents().size(); i++)
 				for (Generic inherited : ((GenericImpl) ((GenericImpl) modified).getComponents().get(i)).getAllInheritings()) {
 					Iterator<Generic> iterator = Statics.valueFilter(((GenericImpl) inherited).holdersIterator(Statics.STRUCTURAL, getCurrentCache().getMetaAttribute(), Statics.MULTIDIRECTIONAL), modified.getValue());
 					if (iterator.hasNext()) {
 						Generic next = iterator.next();
 						if (iterator.hasNext())
							throw new UniqueStructuralValueConstraintViolationException(next.info() + iterator.next().info());
 					}
 				}
 	}
 
 }
