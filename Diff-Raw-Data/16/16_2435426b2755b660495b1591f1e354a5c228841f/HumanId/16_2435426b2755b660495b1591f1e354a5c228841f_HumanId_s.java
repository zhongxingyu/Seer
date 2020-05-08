 package ai.ilikeplaces.logic.validators.unit;
 
 import ai.ilikeplaces.doc.License;
 import ai.ilikeplaces.entities.HumanEquals;
 import ai.ilikeplaces.entities.HumanEqualsFace;
 import ai.ilikeplaces.entities.HumanIdFace;
 import ai.ilikeplaces.entities.Mute;
 import ai.ilikeplaces.util.RefObj;
 import net.sf.oval.Validator;
 import net.sf.oval.configuration.annotation.IsInvariant;
 import net.sf.oval.constraint.Length;
 import net.sf.oval.constraint.NotNull;
 import net.sf.oval.exception.ConstraintsViolatedException;
 
 /**
  * Created by IntelliJ IDEA.
  * User: <a href="http://www.ilikeplaces.com"> http://www.ilikeplaces.com </a>
  * Date: Jan 22, 2010
  * Time: 2:16:43 AM
  */
 
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 public class HumanId extends RefObj<String> implements HumanEqualsFace, HumanIdFace {
 
     public HumanId() {
     }
 
     public HumanId(final String humanId) {
         obj = humanId;
     }
 
     public HumanId(final String humanId, final boolean doValidateAndThrow) {
         if (doValidateAndThrow) {
             /*Threading issue possible here as the validator thread will accesses the object before creation finishes*/
             setObjAsValid(humanId);
         } else {
             obj = humanId;
         }
     }
 
     public HumanId getSelfAsValid(final Validator... validator) {
         return (HumanId) super.getSelfAsValid(validator);
     }
 
 
     @IsInvariant
     @NotNull
     @net.sf.oval.constraint.Email(message = "SORRY! HUMAN ID SHOULD BE AN EMAIL ADDRESS.")
     @Length(max = 255)
     @Override
     public String getObj() {
         return obj;
     }
 
     public String getHumanId() {
         return getObjectAsValid();
     }
 
     @Override
     public void setHumanId(final String humanId__) {
        super.setObj(humanId__);
     }
 
 
     @Override
     public boolean equals(final Object o) {
         if (this == o) {
             return true;
         }
 
         if (o == null) {
             return false;
         }
 
         if (getClass() == o.getClass()) {
            final Mute that = (Mute) o;
             return !(this.getHumanId() == null || that.getHumanId() == null) && this.getHumanId().equals(that.getHumanId());
         } else {
             return HumanEquals.staticMatchHumanId(this,o);
         }
     }
 }
