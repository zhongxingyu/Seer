 package template;
 /**
  * for (u) {
 *  yield (Seq("a") ++ for(u...) ++ Seq("b") ++ for (u) ++ Seq("c"))
  * }
 * to
  * for (u <- coll){
  *   yield "a"
  *   for (u ...) yield f(u)
  *   yield "b"
  *   for (u ...) yield f2(u)
  *   yield "c"
  * }
 *
  *
  *1) Transformation:
  *  OrderShape(pred, shape)
  *  val out = Concat(1 = Seq(Const(_)), 2 = SimpleLoop(sh, i, body), 3 = Seq(Const[_])) =>
 *    1, SimpleLoop(OrderShape(Nil, Const(1)), i , yield Const(_))
  *    2, SimpleLoop(OrderShape(1, sh), i , body) // yields to out now, instead of the intermediate data structure
  *    3, SimpleLoop(OrderShape(2, Const(_)), yield(out) Const(_))
 *
  *2) Fusion which omits the horizontal fusion and preserves the order of statements while fusing
  *
  *3) Order the loops by the OrderShape by inserting dummy effectful statements that order loops. Also cleanup the OrderShape.
  *
  *4) (optional) pick up constant loops and replace them with just their bodies that yield a constant
  *
 * What happens with multiple concats. Fusion must take care of shape fusion and take the last descendant ...
  */
