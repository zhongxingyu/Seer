 /*
  * Sweeper
  * Copyright (C) 2012 Bogdan Pistol
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package gg.pistol.sweeper.core;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 import gg.pistol.sweeper.core.DuplicateTargetGroup;
 import gg.pistol.sweeper.core.OperationTrackingListener;
 import gg.pistol.sweeper.core.SweeperAnalyzer;
 import gg.pistol.sweeper.core.resource.Resource;
 import gg.pistol.sweeper.core.resource.ResourceDirectory;
 import gg.pistol.sweeper.core.resource.ResourceFile;
 
 import java.io.ByteArrayInputStream;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(SweeperAnalyzer.class)
 public class SweeperAnalyzerTest {
 
     private SweeperAnalyzer analyzer;
 
     private SweeperOperationListener listener;
 
     @Before
     public void setUp() throws Exception {
         analyzer = new SweeperAnalyzer();
         listener = mock(OperationTrackingListener.class);
     }
 
     private ResourceFile mockFile(String name, long size, long lastModifiedMillis, String content) throws Exception {
         ResourceFile res = mock(ResourceFile.class);
         when(res.getName()).thenReturn(name);
         when(res.getSize()).thenReturn(size);
         when(res.getModificationDate()).thenReturn(new DateTime(lastModifiedMillis));
         when(res.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes("UTF-8")));
         return res;
     }
 
     private ResourceDirectory mockDirectory(String name, Resource... children) {
         ResourceDirectory res = mock(ResourceDirectory.class);
         when(res.getName()).thenReturn(name);
         ResourceDirectory.ResourceCollectionResponse subresResponse = mock(ResourceDirectory.ResourceCollectionResponse.class);
         when(res.getSubresources()).thenReturn(subresResponse);
         if (children == null) {
             children = new Resource[] {};
         }
         when(subresResponse.getResources()).thenReturn(ImmutableList.copyOf(children));
         when(subresResponse.getExceptions()).thenReturn(Collections.<Exception>emptyList());
         return res;
     }
 
     private boolean areTargetsFromResources(Collection<SweeperTarget> targets, Resource... resources) {
         if (targets.size() != resources.length) {
             return false;
         }
         for (Resource res : resources) {
             boolean contained = false;
             for (SweeperTarget target : targets) {
                 if (target.getResource() == res) {
                     contained = true;
                     break;
                 }
             }
             if (!contained) {
                 return false;
             }
         }
         return true;
     }
 
     @Test
     public void testCompute() throws Exception {
         ResourceFile file1 = mockFile("file1", 1L, 10L, "file1");
         ResourceFile file2 = mockFile("file2", 2L, 11L, "file2");
         ResourceDirectory dir1 = mockDirectory("dir1", file1, file2);
 
         ResourceFile file1Copy = mockFile("file1Copy", 1L, 20L, "file1");
         ResourceFile file2Copy = mockFile("file2Copy", 2L, 21L, "file2");
         ResourceDirectory dir1Copy = mockDirectory("dir1Copy", file1Copy, file2Copy);
         ResourceDirectory dir1CopyUpper = mockDirectory("dir1CopyUpper", dir1Copy);
 
        ResourceFile file3 = mockFile("file3", 5L, 12L, "file3");
         ResourceDirectory dir2 = mockDirectory("dir2", dir1, file3);
 
         ResourceFile emptyFile = mockFile("emptyFile", 0L, 13L, "");
         ResourceDirectory emptyFileDir = mockDirectory("emptyFileDir", emptyFile);
         ResourceDirectory emptyDir = mockDirectory("emptyDir");
         ResourceFile emptyFile2 = mockFile("emptyFile2", 0L, 15L, "");
 
         ResourceDirectory dir3 = mockDirectory("dir3", dir2, dir1CopyUpper, emptyFileDir, emptyDir, emptyFile2);
         ResourceFile file4 = mockFile("file4", 1L, 15L, "file4");
 
         List<DuplicateTargetGroup> dups = analyzer.compute(ImmutableSet.of(dir3, file4), listener);
 
         assertEquals(4, dups.size());
         assertEquals(3L + "24c6f5ec6943e59c19b08dd8f11c73ec8dd3b764", dups.get(0).getHash());
         assertEquals(2L + "cb99b709a1978bd205ab9dfd4c5aaa1fc91c7523", dups.get(1).getHash());
         assertEquals(1L + "60b27f004e454aca81b0480209cce5081ec52390", dups.get(2).getHash());
         assertEquals(0L + "da39a3ee5e6b4b0d3255bfef95601890afd80709", dups.get(3).getHash());
 
         assertTrue(areTargetsFromResources(dups.get(0).getTargets(), dir1, dir1CopyUpper));
         assertTrue(areTargetsFromResources(dups.get(1).getTargets(), file2, file2Copy));
         assertTrue(areTargetsFromResources(dups.get(2).getTargets(), file1, file1Copy));
         assertTrue(areTargetsFromResources(dups.get(3).getTargets(), emptyFileDir, emptyDir, emptyFile2));
 
         verify(listener).updateOperationPhase(SweeperOperationPhase.FILESYSTEM_TRAVERSING);
         verify(listener, atLeastOnce()).updateTargetAction(any(SweeperTargetImpl.class), eq(SweeperTargetAction.EXPAND));
 
         verify(listener).updateOperationPhase(SweeperOperationPhase.SIZE_COMPUTATION);
         verify(listener, atLeastOnce()).updateTargetAction(any(SweeperTargetImpl.class), eq(SweeperTargetAction.COMPUTE_SIZE));
 
         verify(listener).updateOperationPhase(SweeperOperationPhase.SIZE_DEDUPLICATION);
 
         verify(listener).updateOperationPhase(SweeperOperationPhase.HASH_COMPUTATION);
         verify(listener, atLeastOnce()).updateTargetAction(any(SweeperTargetImpl.class), eq(SweeperTargetAction.COMPUTE_HASH));
 
         verify(listener).updateOperationPhase(SweeperOperationPhase.HASH_DEDUPLICATION);
         verify(listener).updateOperationPhase(SweeperOperationPhase.COUNTING);
         verify(listener).updateOperationPhase(SweeperOperationPhase.DUPLICATE_GROUPING);
         verify(listener).updateOperationProgress(100);
 
         SweeperCountImpl count = analyzer.getCount();
         assertEquals(16, count.getTotalTargets());
         assertEquals(8, count.getTotalTargetFiles());
         assertEquals(8, count.getTotalTargetDirectories());
        assertEquals(12L, count.getTotalSize());
 
         assertTrue(ImmutableSet.of(5, 6, 7).contains(count.getDuplicateTargets()));
         assertTrue(ImmutableSet.of(3, 4).contains(count.getDuplicateTargetFiles()));
         assertTrue(ImmutableSet.of(2, 3, 4).contains(count.getDuplicateTargetDirectories()));
         assertEquals(3L, count.getDuplicateSize());
     }
 
     @Test
     public void testComputeException() throws Exception {
         try {
             analyzer.compute(null, listener);
             fail();
         } catch (NullPointerException e) {
             // expected
         }
 
         try {
             analyzer.compute(Collections.<Resource>emptySet(), null);
             fail();
         } catch (NullPointerException e) {
             // expected
         }
 
         try {
             analyzer.compute(Collections.<Resource>emptySet(), listener);
             fail();
         } catch (IllegalArgumentException e) {
             // expected
         }
     }
 
     @Test
     public void testAbort() throws Exception {
         ResourceDirectory dir = mockDirectory("dir");
         Resource root = mockDirectory("root", dir);
         analyzer = PowerMockito.spy(analyzer);
         PowerMockito.when(analyzer, "checkAbortFlag").thenThrow(new SweeperAbortException());
 
         try {
             analyzer.compute(ImmutableSet.of(root), listener);
             fail();
         } catch (SweeperAbortException e) {
             // expected
             assertNull(analyzer.getCount());
         }
     }
 
 }
