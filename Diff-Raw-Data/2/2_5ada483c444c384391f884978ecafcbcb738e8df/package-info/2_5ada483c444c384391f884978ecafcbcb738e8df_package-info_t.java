 /**
  * This package contains the square dance choreography engine, and the basic
  * types and interfaces required to communicate with it.
  *
  * @doc.test Test basic call database functionality:
  *  js> CallDB.INSTANCE.parse(Program.BASIC, "double pass thru").expand()
  *  (In 4 (Seq (Apply tandem (Apply pass thru))))
  *
  * @doc.test Calls with arguments:
  *  js> importPackage(net.cscott.sdr.util) // for Fraction
  *  js> importPackage(net.cscott.sdr.calls.ast) // for Apply
  *  js> sqthr = CallDB.INSTANCE.lookup("square thru")
  *  square thru[basic]
  *  js> sqthr.apply(Apply.makeApply("square thru", Fraction.valueOf("1 1/2")))
 *  (Opt (From [FACING COUPLES] (If (Condition and (Condition greater (Condition 3/2) (Condition 0)) (Condition not (Condition greater (Condition 3/2) (Condition 1)))) (Seq (Apply _fractional (Apply 3/2) (Apply _sq_thr_part_a))))) (From [FACING COUPLES] (If (Condition greater (Condition 3/2) (Condition 1)) (Seq (Part false (Seq (Apply and (Apply _sq_thr_part_b) (Apply left (Apply square thru (Apply _subtract_num (Apply 3/2) (Apply 1)))))))))))
  *
  * @doc.test Call fractionalization:
  *  js> importPackage(net.cscott.sdr.util) // for Fraction
  *  js> importPackage(net.cscott.sdr.calls.ast) // for Apply
  *  js> a = Apply.makeApply("run", Apply.makeApply("boy"))
  *  (Apply run (Apply boy))
  *  js> a.expand()
  *  (In 4 (Opt (From [GENERAL LINE, OR(RH BOX,LH BOX), COUPLE, RH MINIWAVE, LH MINIWAVE] (Par (Select [BOY] (Par (Select [BEAU] (Seq (Prim 1, 1, right, 1) (Prim 1, 1, right, 1))) (Select [BELLE] (Seq (Prim -1, 1, left, 1) (Prim -1, 1, left, 1))))) (Select [ALL] (Par (Select [BEAU] (Seq (Prim 1, 0, none, 1, SASHAY_START, SASHAY_FINISH) (Prim 1, 0, none, 1, SASHAY_START, SASHAY_FINISH))) (Select [BELLE] (Seq (Prim -1, 0, none, 1, SASHAY_START, SASHAY_FINISH) (Prim -1, 0, none, 1, SASHAY_START, SASHAY_FINISH)))))))))
  *  js> a = Apply.makeApply("_fractional", Apply.makeApply("1/2"), a)
  *  (Apply _fractional (Apply 1/2) (Apply run (Apply boy)))
  *  js> a.expand()
  *  (In 2 (Opt (From [GENERAL LINE, OR(RH BOX,LH BOX), COUPLE, RH MINIWAVE, LH MINIWAVE] (Par (Select [BOY] (Par (Select [BEAU] (Seq (Prim 1, 1, right, 1))) (Select [BELLE] (Seq (Prim -1, 1, left, 1))))) (Select [ALL] (Par (Select [BEAU] (Seq (Prim 1, 0, none, 1, SASHAY_START, SASHAY_FINISH))) (Select [BELLE] (Seq (Prim -1, 0, none, 1, SASHAY_START, SASHAY_FINISH)))))))))
  */
 package net.cscott.sdr.calls;
