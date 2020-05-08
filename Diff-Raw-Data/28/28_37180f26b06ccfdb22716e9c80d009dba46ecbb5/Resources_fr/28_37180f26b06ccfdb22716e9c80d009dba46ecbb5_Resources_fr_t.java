 package net.sourceforge.importscrubber;
 
 public class Resources_fr extends Resources
 
 {
 
     private static final Object[][] contents = {
 
                 {FILE_BROWSER_TITLE, "Note: les fichiers binaires (.class) et les fichiers sources doivent tre dans le mme rpertoire"},
 
                 {VERSION_ID, "Nettoyeur d'imports 1.4.2"},
 
                 {BROWSE_LABEL, "Naviguer"},
 
                 {GO_LABEL, "Aller"},
 
                 {HELP_LABEL, "Aide"},
 
                 {EXIT_LABEL, "Quitter"},
 
                 {ABOUT_LABEL, " propos"},
 
                 {RECURSE_LABEL, "Rcursivement"},
 
                 {FILE_LABEL, "Fichiers"},
 
                 {FIND_FILES_LABEL, "Trouver les fichiers"},
 
                 {ALL_DONE, "C'est fini!"},
 
                 {APP_NAME, "Nettoyeur d'imports"},
 
                 {HELP_MESSAGE, "                     Nettoyeur d'imports\nC'est un utilitaire pour nettoyer les imports. Pour l'utiliser:\n1) S'assurer que les fichiers sources et les fichiers binaires (.class) sont dans le mme rpertoire. \n2) Slectionner le fichier contenant du source\n3) Cliquer sur \"Trouver les fichiers\"\n4) Cliquer sur \"Excuter\"\n Nettoyeur d'imports va mouliner pour quelques secondes et montrer un dialogue pour dire que c'est termin.\n Pour traiter plusieurs fichiers, simplement slectionner un rpertoire et cocher le choix \"Rcursivement\".\nQuestions? Commentaires? Contacter tomcopeland@users.sourceforge.net"},
 
                 {BREAK_EACH_PACKAGE, "Stopper pour chaque package"},
 
                 {BREAK_NONE, "Pas d'arrts"},
 
                {ERR_NOT_DIR, " n'est pas un rpertoire!"},
 
                {ERR_DIR_NOT_EXIST, " n'existe past!"},
 
                 {ERR_CLASS_FILE_MUST_EXIST, "Le fichier binaire (.class) doit exister:"},
 
                 {ERR_MUST_NOT_BE_DIR, "Le fichier d'entre ne peut pas tre un rpertoire: "},
 
                 {ERR_UNABLE_TO_FINISH, "Incapable de finir  cause de:"}
 
             };
 
 
 
     public Object[][] getContents()
 
     {
 
         return contents;
 
     }
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
