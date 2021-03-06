 package de.jroene.vrapper.vim.token;
 
 import de.jroene.vrapper.vim.LineInformation;
 import de.jroene.vrapper.vim.Platform;
 import de.jroene.vrapper.vim.VimEmulator;
 import de.jroene.vrapper.vim.VimUtils;
 import de.jroene.vrapper.vim.action.Action;
 
 /**
  * Deletes a region of text.
  *
  * @author Matthias Radig
  */
 public class Delete extends AbstractLineAwareEdit {
 
     public Delete() {
         super();
     }
 
     public Delete(int target, Move subject, Number multiplier) {
         super(target, subject, multiplier);
     }
 
     @Override
     public Action getAction() {
         return isLineDeletion() ? new LineDeleteAction() : new DeleteAction();
     }
 
     public class LineDeleteAction extends LineEditAction {
        LineInformation originalLine;
         @Override
         protected void doEdit(VimEmulator vim, LineInformation originalLine, int start,
                 int end) {
             Platform pl = vim.getPlatform();
             pl.replace(start, end - start, "");
            this.originalLine = originalLine;
        }
        @Override
        protected void afterEdit(VimEmulator vim, LineInformation startLine,
                LineInformation endLine) {
            super.afterEdit(vim, startLine, endLine);
            Platform pl = vim.getPlatform();
             // re-fetch line information after change
             pl.setPosition(VimUtils.getSOLAwarePositionAtLine(vim, originalLine.getNumber()));
         }
     }
 
     public class DeleteAction extends EditAction {
         @Override
         protected void doEdit(VimEmulator vim, int originalPosition, int start,
                 int end) {
             vim.getPlatform().replace(start, end - start, "");
        }

        @Override
        protected void afterEdit(VimEmulator vim, int start, int end) {
            super.afterEdit(vim, start, end);
             vim.getPlatform().setPosition(start);
         }
     }
 
     static class BufferNeutral extends Delete {
 
         public BufferNeutral() {
             super();
         }
 
         public BufferNeutral(int target, Move subject, Number multiplier) {
             super(target, subject, multiplier);
         }
 
         @Override
         public Action getAction() {
             return isLineDeletion() ? new NeutralLineDeleteAction() : new NeutralDeleteAction();
         }
 
         private class NeutralLineDeleteAction extends LineDeleteAction {
 
             @Override
             protected void beforeEdit(VimEmulator vim, LineInformation start, LineInformation end) {
                 // do nothing
             }
         }
 
         private class NeutralDeleteAction extends DeleteAction {
 
             @Override
             protected void beforeEdit(VimEmulator vim, int start, int end) {
                 // do nothing
             }
         }
     }
 
 }
