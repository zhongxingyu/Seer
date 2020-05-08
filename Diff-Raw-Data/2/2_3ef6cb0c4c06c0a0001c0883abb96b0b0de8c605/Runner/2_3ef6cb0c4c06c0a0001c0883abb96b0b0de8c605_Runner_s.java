 // -*- mode: Java; c-basic-offset: 3; tab-width: 8; indent-tabs-mode: nil -*-
 // Copyright (C) 2008 Andreas Krey, Ulm, Germany <a.krey@gmx.de>
 
 package gloop;
 
 public class Runner {
    private String [] strings;
    private byte [] code;
    private int entry;
 
    private static final Object nullval = new Object () {
          public String toString () { return "nullval"; }
       };
 
    public Runner (String [] str, byte [] a, int e) {
       strings = str;
       code = a;
       entry = e;
    }
 
    private static class Frame {
       public Frame stat;
       public Frame dyn;
       public int base; // Number of saved stack cells
       public Object [] data;
       public int pc;
    }
 
    private static class Fun {
       public Frame ctx;
       public int start;
       public int nargs;
    }
 
    public void run () {
       Object [] stack = new Object [100]; /// XXX Need some measure...
       int sp = 0;
 
       int pc = entry;
       Object acc = null;
       Frame tmp = null;
       int param = 0;
       Frame fp = new Frame ();
       fp.base = 0;
       fp.data = new Object [100];
       fp.pc = -1;
       while (true) {
          int c = code [pc ++] & 255;
          if (c >= 240) {
            param = (param << 16) | (c & 15);
             continue;
          }
          String op = Code.getCode (c);
          for (int i = 0; i < sp; i ++) {
             System.out.print (" " + stack [i]);
          }
          System.out.println (" * " + acc);
          if (param != 0) {
             System.out.println ("        [" + (pc - 1) + "] op=" + op +
                                 " (" + param + ")");
          } else {
             System.out.println ("        [" + (pc - 1) + "] op=" + op);
          }
          if (op == "nop") {
          } else if (op == "up") {
             for (tmp = fp; param > 0; param --) {
                tmp = tmp.stat;
             }
          } else if (op == "lstore") {
             fp.data [param + fp.base] = acc;
             param = 0;
          } else if (op == "store") {
             tmp.data [param + tmp.base] = acc;
             param = 0;
          } else if (op == "lload") {
             acc = fp.data [param + fp.base];
             param = 0;
          } else if (op == "load") {
             acc = tmp.data [param + tmp.base];
             param = 0;
          } else if (op == "print") {
             System.out.println ("PRINT: " + acc);
          } else if (op == "strval") {
             acc = strings [param];
             param = 0;
          } else if (op == "numval") {
             acc = new Integer (param);
             param = 0;
          } else if (op == "nullval") {
             acc = nullval;
          } else if (op == "fun") {
             Fun f = new Fun ();
             f.ctx = fp;
             f.start = ((Integer)acc).intValue ();
             f.nargs = param;
             param = 0;
             acc = f;
          } else if (op == "push") {
             stack [sp ++ ] = acc;
          } else if (op == "swap") {
             Object h = acc;
             acc = stack [sp - 1];
             stack [sp - 1] = h;
          } else if (op == "mult") {
             acc = new Integer (((Integer)acc).intValue () *
                                ((Integer)stack [-- sp]).intValue ());
          } else if (op == "call") {
             Fun a = (Fun)acc;
             Frame f = new Frame ();
             if (a.nargs != param) {
                throw new IllegalArgumentException ("mismatching arg count");
             }
             f.data = new Object [100];
             f.pc = pc;
             f.base = sp - param; // We take the params from the stack!
             f.dyn = fp;
             f.stat = a.ctx;
             for (int i = 0; i < sp; i ++) {
                System.out.println ("COPY " + stack [i]);
                f.data [i] = stack [i];
             }
             pc = a.start;
             sp = 0;
             param = 0;
             fp = f;
          } else if (op == "ret") {
             pc = fp.pc;
             sp = 0;
             for (int i = 0; i < fp.base; i ++) {
                stack [sp ++] = fp.data [i];
             }
             fp = fp.dyn;
          } else if (op == "stop") {
             break;
          } else {
             throw new IllegalArgumentException ("unknown op " + op +
                                                 " (" + c + ")");
          }
       }
    }
 }
