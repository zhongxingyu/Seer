 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.test.uri;
 
 import org.waterken.uri.URI;
 
 /**
  * Package test.
  */
 public final class
 Main {
    private Main() {}
 
     static public void
     main(final String[] args) throws Exception {
         
         // test custom URIs
         if (args.length >= 1) {
             final String base = args[0];
             for (int i = 1; i != args.length; ++i) {
                 System.out.println(URI.resolve(base, args[i]));
             }
         }
 
         testAuthority();
         testPath();
         testStandard();
         testRelate();
     }
     
     static private void
     test(final String calculated, final String expected) {
         if (!calculated.equals(expected)) {
             throw new RuntimeException(calculated + " != " + expected);
         }
     }
     
     static private void
     testAuthority() {
         test(URI.authority("http://a/"),    "a");
         test(URI.authority("http://a"),     "a");
         test(URI.authority("http://a?q"),   "a");
         test(URI.authority("http://a#f"),   "a");
         test(URI.authority("http://a?q#f"), "a");
         test(URI.authority("http://a?q/"),  "a");
         test(URI.authority("http://a#f/"),  "a");
 
         test(URI.authority("//a/"),         "a");
         test(URI.authority("//a"),          "a");
         test(URI.authority("//a?q"),        "a");
         test(URI.authority("//a#f"),        "a");
         test(URI.authority("//a?q#f"),      "a");
         test(URI.authority("//a?q/"),       "a");
         test(URI.authority("//a#f/"),       "a");
 
         test(URI.authority("mailto:p"),     "");
         test(URI.authority("mailto:p?q"),   "");
         test(URI.authority("mailto:p#f"),   "");
         test(URI.authority("mailto:p?q#f"), "");
         test(URI.authority("mailto:p?q/"),  "");
         test(URI.authority("mailto:p#f/"),  "");
 
         test(URI.authority("p"),            "");
         test(URI.authority("p?q"),          "");
         test(URI.authority("p#f"),          "");
         test(URI.authority("p?q#f"),        "");
         test(URI.authority("p?q/"),         "");
         test(URI.authority("p#f/"),         "");
 
         test(URI.authority("/p"),           "");
         test(URI.authority("./p"),          "");
         test(URI.authority("../p"),         "");
         test(URI.authority("?p"),           "");
         test(URI.authority("#p"),           "");
     }
     
     static private void
     testPath() {
         test(URI.path("http://a"),          "");
         test(URI.path("http://a?q"),        "");
         test(URI.path("http://a#f"),        "");
         test(URI.path("http://a?q#f"),      "");
         test(URI.path("http://a?q/"),       "");
         test(URI.path("http://a#f/"),       "");
         test(URI.path("http://a/"),         "");
         test(URI.path("http://a/?q"),       "");
         test(URI.path("http://a/#f"),       "");
         test(URI.path("http://a/?q#f"),     "");
         test(URI.path("http://a/?q/"),      "");
         test(URI.path("http://a/#f/"),      "");
 
         test(URI.path("//a"),               "");
         test(URI.path("//a?q"),             "");
         test(URI.path("//a#f"),             "");
         test(URI.path("//a?q#f"),           "");
         test(URI.path("//a?q/"),            "");
         test(URI.path("//a#f/"),            "");
         test(URI.path("//a/"),              "");
         test(URI.path("//a/?q"),            "");
         test(URI.path("//a/#f"),            "");
         test(URI.path("//a/?q#f"),          "");
         test(URI.path("//a/?q/"),           "");
         test(URI.path("//a/#f/"),           "");
         
         test(URI.path("http://a/p"),        "p");
         test(URI.path("http://a/p?q"),      "p");
         test(URI.path("http://a/p#f"),      "p");
         test(URI.path("http://a/p?q#f"),    "p");
         test(URI.path("http://a/p?q/"),     "p");
         test(URI.path("http://a/p#f/"),     "p");
 
         test(URI.path("//a/p"),             "p");
         test(URI.path("//a/p?q"),           "p");
         test(URI.path("//a/p#f"),           "p");
         test(URI.path("//a/p?q#f"),         "p");
         test(URI.path("//a/p?q/"),          "p");
         test(URI.path("//a/p#f/"),          "p");
 
         test(URI.path("mailto:"),           "");
         test(URI.path("mailto:?q"),         "");
         test(URI.path("mailto:#f"),         "");
         test(URI.path("mailto:?q#f"),       "");
         test(URI.path("mailto:?q/"),        "");
         test(URI.path("mailto:#f/"),        "");
 
         test(URI.path("mailto:p"),          "p");
         test(URI.path("mailto:p?q"),        "p");
         test(URI.path("mailto:p#f"),        "p");
         test(URI.path("mailto:p?q#f"),      "p");
         test(URI.path("mailto:p?q/"),       "p");
         test(URI.path("mailto:p#f/"),       "p");
 
         test(URI.path("p"),                 "p");
         test(URI.path("p?q"),               "p");
         test(URI.path("p#f"),               "p");
         test(URI.path("p?q#f"),             "p");
         test(URI.path("p?q/"),              "p");
         test(URI.path("p#f/"),              "p");
 
         test(URI.path("/p"),                "p");
         test(URI.path("./p"),               "./p");
         test(URI.path("../p"),              "../p");
         test(URI.path("?a"),                "");
         test(URI.path("#a"),                "");
     }
     
     static private void
     testStandard() {
         final String base = "http://a/b/c/d;p?q#f";
 
         // normal examples
         test(URI.resolve(base, "g:h"),      "g:h");
         test(URI.resolve(base, "g"),        "http://a/b/c/g");
         test(URI.resolve(base, "./g"),      "http://a/b/c/g");
         test(URI.resolve(base, "g/"),       "http://a/b/c/g/");
         test(URI.resolve(base, "/g"),       "http://a/g");
         test(URI.resolve(base, "//g"),      "http://g");
         test(URI.resolve(base, "?y"),       "http://a/b/c/d;p?y");
         test(URI.resolve(base, "?y#s"),     "http://a/b/c/d;p?y#s");
         test(URI.resolve(base, "g?y"),      "http://a/b/c/g?y");
         test(URI.resolve(base, "#s"),       "http://a/b/c/d;p?q#s");
         test(URI.resolve(base, "g#s"),      "http://a/b/c/g#s");
         test(URI.resolve(base, "g?y#s"),    "http://a/b/c/g?y#s");
         test(URI.resolve(base, ";x"),       "http://a/b/c/;x");
         test(URI.resolve(base, "g;x"),      "http://a/b/c/g;x");
         test(URI.resolve(base, "g;x?y#s"),  "http://a/b/c/g;x?y#s");
         test(URI.resolve(base, ""),         "http://a/b/c/d;p?q");
         test(URI.resolve(base, "."),        "http://a/b/c/");
         test(URI.resolve(base, "./"),       "http://a/b/c/");
         test(URI.resolve(base, ".."),       "http://a/b/");
         test(URI.resolve(base, "../"),      "http://a/b/");
         test(URI.resolve(base, "../g"),     "http://a/b/g");
         test(URI.resolve(base, "../.."),    "http://a/");
         test(URI.resolve(base, "../../"),   "http://a/");
         test(URI.resolve(base, "../../g"),  "http://a/g");
 
         // abnormal examples
         test(URI.resolve(base, "../../.."),         "http://a/");
         test(URI.resolve(base, "../../../"),        "http://a/");
         test(URI.resolve(base, "../../../.."),      "http://a/");
         test(URI.resolve(base, "../../../../"),     "http://a/");
         test(URI.resolve(base, "../../../../."),    "http://a/");
         test(URI.resolve(base, "../../../g"),       "http://a/g");
         test(URI.resolve(base, "../../../../g"),    "http://a/g");
         test(URI.resolve(base, "/./g"),             "http://a/g");
         test(URI.resolve(base, "/../g"),            "http://a/g");
         test(URI.resolve(base, "g."),               "http://a/b/c/g.");
         test(URI.resolve(base, ".g"),               "http://a/b/c/.g");
         test(URI.resolve(base, "g.."),              "http://a/b/c/g..");
         test(URI.resolve(base, "..g"),              "http://a/b/c/..g");
         test(URI.resolve(base, "./../g"),           "http://a/b/g");
         test(URI.resolve(base, "./g/."),            "http://a/b/c/g/");
         test(URI.resolve(base, "g/./h"),            "http://a/b/c/g/h");
         test(URI.resolve(base, "g/../h"),           "http://a/b/c/h");
         test(URI.resolve(base, "g;x=1/./y"),        "http://a/b/c/g;x=1/y");
         test(URI.resolve(base, "g;x=1/../y"),       "http://a/b/c/y");
         test(URI.resolve(base, "g?y/./x"),          "http://a/b/c/g?y/./x");
         test(URI.resolve(base, "g?y/../x"),         "http://a/b/c/g?y/../x");
         test(URI.resolve(base, "g#s/./x"),          "http://a/b/c/g#s/./x");
         test(URI.resolve(base, "g#s/../x"),         "http://a/b/c/g#s/../x");
     }
     
     static private void
     testRelate() {
         final String base = "http://a/b/c/d;p?q#f";
         
         test(URI.relate(base, "http://a/b/c/d;p?q#f"),  "./d;p?q#f");
         test(URI.relate(base, "http://a/b/c/d;p?q#f2"), "./d;p?q#f2");
         test(URI.relate(base, "http://a/b/c/d;p?q"),    "./d;p?q");
         test(URI.relate(base, "http://a/b/c/d;p"),      "./d;p");
         test(URI.relate(base, "http://a/b/c/"),         "./");
         test(URI.relate(base, "http://a/b/c"),          "../c");
         test(URI.relate(base, "http://a/b/"),           "../");
         test(URI.relate(base, "http://a/b"),            "../../b");
         test(URI.relate(base, "http://a/"),             "../../");
 
         test(URI.relate(base, "http://a2/"),            "http://a2/");
         test(URI.relate(base, "https://a/"),            "https://a/");
     }
 }
