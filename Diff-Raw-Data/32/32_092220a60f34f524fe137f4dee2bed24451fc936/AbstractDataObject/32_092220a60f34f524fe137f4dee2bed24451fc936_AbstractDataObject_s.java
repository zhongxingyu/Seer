 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.framework.standard;
 import static org.apache.commons.lang3.builder.ToStringStyle.*;
 import java.io.Serializable;
 import org.apache.commons.lang3.builder.CompareToBuilder;
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 import org.apache.commons.lang3.builder.ToStringBuilder;
 /**
  * 汎用データオブジェクト
  * <p>
  * データオブジェクトの基本的な機能を提供する。 参照オブジェクトと値オブジェクトは本クラスを継承すること。
  * </p>
  * @param <T> データオブジェクト型
  * @author nilcy
  */
 public abstract class AbstractDataObject<T extends AbstractDataObject<T>> implements Serializable,
     Comparable<T> {
     /** 識別番号 */
     private static final long serialVersionUID = -4438164461402375117L;
     /** Constructor. */
     public AbstractDataObject() {
     }
     /**
      * オブジェクトの文字列表現
      * <p>
     * インスタンスをもとに短縮形式で自動生成する。
      * </p>
      * @return オブジェクトの文字列表現
      */
     @Override
     public String toString() {
         return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
     }
    /** {@inheritDoc} */
     @Override
     public boolean equals(final Object aObj) {
         return EqualsBuilder.reflectionEquals(this, aObj, true);
     }
    /** {@inheritDoc} */
     @Override
     public int hashCode() {
         return HashCodeBuilder.reflectionHashCode(this, true);
     }
    /** {@inheritDoc} */
     @Override
     public int compareTo(final T aObj) {
         return CompareToBuilder.reflectionCompare(this, aObj, true);
     }
 }
