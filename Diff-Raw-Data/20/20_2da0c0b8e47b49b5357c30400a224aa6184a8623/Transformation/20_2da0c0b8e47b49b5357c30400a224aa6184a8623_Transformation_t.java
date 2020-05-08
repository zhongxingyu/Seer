 package template;
 /**
  * for (u) {
 *  for (x <- (Seq("a") ++ for(u...) ++ Seq("b") ++ for (u...) ++ Seq("c")))
 *    yield x
  * }
 * transforms to
  * for (u <- coll){
  *   yield "a"
  *   for (u ...) yield f(u)
  *   yield "b"
  *   for (u ...) yield f2(u)
  *   yield "c"
  * }
 * Which is exactly how the code looks when you do templating by hand (buf is always passed as an argument of methods (or available in the scope))
  *
  *1) Transformation:
  *  OrderShape(pred, shape)
  *  val out = Concat(1 = Seq(Const(_)), 2 = SimpleLoop(sh, i, body), 3 = Seq(Const[_])) =>
 *    1, SimpleLoop(OrderShape(Nil, Const(1)), i , yield Const(_)) 
  *    2, SimpleLoop(OrderShape(1, sh), i , body) // yields to out now, instead of the intermediate data structure
  *    3, SimpleLoop(OrderShape(2, Const(_)), yield(out) Const(_))
 *    // 1,2,3 can be fused vertically with loops below. 2 can be fused with loops above.
 * 
  *2) Fusion which omits the horizontal fusion and preserves the order of statements while fusing
  *
  *3) Order the loops by the OrderShape by inserting dummy effectful statements that order loops. Also cleanup the OrderShape.
  *
  *4) (optional) pick up constant loops and replace them with just their bodies that yield a constant
  *
 * Notes: What happens with multiple concats? Fusion must take care of the custom shape. Probably we will need another
 * hook in the fusion algorithm that dictates how shapes are fused.
  */
