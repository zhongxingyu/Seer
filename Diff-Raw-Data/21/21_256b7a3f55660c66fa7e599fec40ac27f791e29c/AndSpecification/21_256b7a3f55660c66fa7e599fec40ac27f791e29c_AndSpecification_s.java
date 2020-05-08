 package org.satokit.sample.spec;
 
 import org.apache.commons.lang.Validate;
 
 /**
  * 2つの仕様オブジェクトの論理積を表現するオブジェクト.
  * @param <T> 仕様検証対象のオブジェクト型
  */
 public class AndSpecification<T> implements Specification<T> {
     /** 左辺となる仕様 */
     private Specification<T> left;
     /** 右辺となる仕様 */
     private Specification<T> right;
 
     /**
      * 論理積を取る仕様オブジェクトを2つ与えて論理積仕様インスタンスを生成する.
      *
      * @param left 左辺
      * @param right 右辺
     * @throws IllegalArgumentException {@code left, right}のいずれかでも{@code null}だった場合.
      */
     public AndSpecification(Specification<T> left, Specification<T> right) {
         Validate.notNull(left, "left must not be null.");
         Validate.notNull(right, "right must not be null.");
         this.left = left;
         this.right = right;
     }
 
     @Override
     public boolean isSatisfied(T target) {
         Validate.notNull(target, "target must not be null.");
         return left.isSatisfied(target) && right.isSatisfied(target);
     }
 }
