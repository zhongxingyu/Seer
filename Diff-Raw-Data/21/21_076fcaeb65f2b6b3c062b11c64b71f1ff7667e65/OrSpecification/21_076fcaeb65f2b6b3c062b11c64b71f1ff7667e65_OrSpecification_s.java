 package org.satokit.sample.spec;
 
 import org.apache.commons.lang.Validate;
 
 /**
  * 2つの仕様オブジェクトの論理和を表現するクラス.
  * @param <T> 仕様検証対象オブジェクトの型
  */
 public class OrSpecification<T> implements Specification<T> {
     /** 左辺となる仕様 */
     private final Specification<T> left;
     /** 右辺となる仕様 */
     private final Specification<T> right;
 
     /**
      * 論理和を取る仕様オブジェクトを2つ与えて論理和仕様インスタンスを生成する.
      *
      * @param left 左辺
      * @param right 右辺
     * @throws IllegalArgumentException {@code left, right}のいずれかでも{@code null}の場合.
      */
     public OrSpecification(Specification<T> left, Specification<T> right) {
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
      */
     @Override
     public boolean isSatisfied(T target) {
         Validate.notNull(target, "target must not be null.");
         return left.isSatisfied(target)
                 || right.isSatisfied(target);
     }
 }
