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
     * @throws IllegalArgumentException
     *          {@code left == null || right == null}
      */
     public AndSpecification(Specification<T> left, Specification<T> right) {
         Validate.notNull(left, "left must not be null.");
         Validate.notNull(right, "right must not be null.");
         this.left = left;
         this.right = right;
     }
 
    /**
     * 対象のオブジェクトがこのオブジェクトが表現する仕様を満たしているか検証する.
     *
     * @param target 仕様検証対象のオブジェクト
     * @return {@code target}が仕様を満たしている場合{@code true}, 満たしていない場合{@code false}.
     * @throws IllegalArgumentException {@code target == null}
     */
     @Override
     public boolean isSatisfied(T target) {
         Validate.notNull(target, "target must not be null.");
         return left.isSatisfied(target) && right.isSatisfied(target);
     }
 }
